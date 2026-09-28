package com.luck.report.jdbc;

import com.luck.report.jdbc.node.BindNode;
import com.luck.report.jdbc.node.ChooseNode;
import com.luck.report.jdbc.node.ForeachNode;
import com.luck.report.jdbc.node.IfNode;
import com.luck.report.jdbc.node.IncludeNode;
import com.luck.report.jdbc.node.SqlNode;
import com.luck.report.jdbc.node.StaticTextNode;
import com.luck.report.jdbc.node.TrimNode;
import com.luck.report.jdbc.node.WhenBranch;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 解析类 MyBatis 的 mapper XML
 */
public class LuckSqlParser {

    /**
     * 解析单个 mapper XML 输入流
     *
     * @param input XML 流，调用方关闭
     * @return statementId → SqlStatement
     */
    public Map<String, SqlStatement> parse(InputStream input) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            Document document = factory.newDocumentBuilder().parse(input);
            Element root = document.getDocumentElement();
            if (!"mapper".equals(root.getTagName())) {
                throw new IllegalArgumentException("Root element must be <mapper>");
            }
            String namespace = root.getAttribute("namespace");
            if (namespace == null || namespace.trim().isEmpty()) {
                throw new IllegalArgumentException("mapper namespace is required");
            }

            Map<String, Element> sqlFragments = collectSqlFragments(root);
            Map<String, SqlStatement> result = new LinkedHashMap<String, SqlStatement>();
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node node = children.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                Element element = (Element) node;
                SqlStatement.StatementType type = resolveType(element.getTagName());
                if (type == null) {
                    continue;
                }
                String id = element.getAttribute("id");
                if (id == null || id.trim().isEmpty()) {
                    throw new IllegalArgumentException("statement id is required in namespace " + namespace);
                }
                String fullId = namespace + "." + id;
                if (result.containsKey(fullId)) {
                    throw new IllegalArgumentException("Duplicate SQL id in mapper: " + fullId);
                }
                List<SqlNode> nodes = parseChildren(element, sqlFragments, new HashSet<String>());
                result.put(fullId, new SqlStatement(fullId, type, nodes));
            }
            return result;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse mapper XML", ex);
        }
    }

    private Map<String, Element> collectSqlFragments(Element root) {
        Map<String, Element> fragments = new HashMap<String, Element>();
        NodeList children = root.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element element = (Element) node;
            if (!"sql".equals(element.getTagName())) {
                continue;
            }
            String id = element.getAttribute("id");
            if (id == null || id.trim().isEmpty()) {
                throw new IllegalArgumentException("<sql> fragment id is required");
            }
            if (fragments.containsKey(id)) {
                throw new IllegalArgumentException("Duplicate <sql> fragment id: " + id);
            }
            fragments.put(id, element);
        }
        return fragments;
    }

    private SqlStatement.StatementType resolveType(String tag) {
        if ("select".equals(tag)) {
            return SqlStatement.StatementType.SELECT;
        }
        if ("insert".equals(tag)) {
            return SqlStatement.StatementType.INSERT;
        }
        if ("update".equals(tag)) {
            return SqlStatement.StatementType.UPDATE;
        }
        if ("delete".equals(tag)) {
            return SqlStatement.StatementType.DELETE;
        }
        return null;
    }

    private List<SqlNode> parseChildren(Element parent, Map<String, Element> sqlFragments, Set<String> includeStack) {
        List<SqlNode> nodes = new ArrayList<SqlNode>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            short type = node.getNodeType();
            if (type == Node.TEXT_NODE || type == Node.CDATA_SECTION_NODE) {
                String text = node.getTextContent();
                if (text != null && !text.isEmpty()) {
                    nodes.add(new StaticTextNode(text));
                }
            } else if (type == Node.ELEMENT_NODE) {
                nodes.add(parseElement((Element) node, sqlFragments, includeStack));
            }
        }
        return nodes;
    }

    private SqlNode parseElement(Element element, Map<String, Element> sqlFragments, Set<String> includeStack) {
        String tag = element.getTagName();
        if ("if".equals(tag)) {
            return new IfNode(element.getAttribute("test"), parseChildren(element, sqlFragments, includeStack));
        }
        if ("foreach".equals(tag)) {
            String indexAttr = element.getAttribute("index");
            if (indexAttr != null && !indexAttr.isEmpty()) {
                throw new IllegalArgumentException(
                        "<foreach index> is not supported; remove index attribute");
            }
            return new ForeachNode(
                    element.getAttribute("collection"),
                    element.getAttribute("item"),
                    element.getAttribute("open"),
                    element.getAttribute("close"),
                    element.getAttribute("separator"),
                    parseChildren(element, sqlFragments, includeStack));
        }
        if ("trim".equals(tag)) {
            return new TrimNode(
                    emptyToNull(element.getAttribute("prefix")),
                    emptyToNull(element.getAttribute("suffix")),
                    emptyToNull(element.getAttribute("prefixOverrides")),
                    emptyToNull(element.getAttribute("suffixOverrides")),
                    parseChildren(element, sqlFragments, includeStack));
        }
        if ("where".equals(tag)) {
            return TrimNode.where(parseChildren(element, sqlFragments, includeStack));
        }
        if ("set".equals(tag)) {
            return TrimNode.set(parseChildren(element, sqlFragments, includeStack));
        }
        if ("bind".equals(tag)) {
            return new BindNode(element.getAttribute("name"), element.getAttribute("value"));
        }
        if ("choose".equals(tag)) {
            return parseChoose(element, sqlFragments, includeStack);
        }
        if ("include".equals(tag)) {
            return parseInclude(element, sqlFragments, includeStack);
        }
        throw new IllegalArgumentException("Unsupported tag: <" + tag + ">");
    }

    private SqlNode parseInclude(Element element, Map<String, Element> sqlFragments, Set<String> includeStack) {
        String refid = element.getAttribute("refid");
        if (refid == null || refid.trim().isEmpty()) {
            throw new IllegalArgumentException("<include> refid is required");
        }
        // 支持 namespace.id 或本地 id
        String localId = refid;
        int dot = refid.lastIndexOf('.');
        if (dot >= 0) {
            localId = refid.substring(dot + 1);
        }
        Element fragment = sqlFragments.get(localId);
        if (fragment == null) {
            fragment = sqlFragments.get(refid);
        }
        if (fragment == null) {
            throw new IllegalArgumentException("Unknown <sql> fragment refid: " + refid);
        }
        if (!includeStack.add(localId)) {
            throw new IllegalArgumentException("Circular <include> detected: " + localId);
        }
        try {
            return new IncludeNode(parseChildren(fragment, sqlFragments, includeStack));
        } finally {
            includeStack.remove(localId);
        }
    }

    private SqlNode parseChoose(Element element, Map<String, Element> sqlFragments, Set<String> includeStack) {
        List<WhenBranch> whens = new ArrayList<WhenBranch>();
        List<SqlNode> otherwise = null;
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element child = (Element) node;
            if ("when".equals(child.getTagName())) {
                whens.add(new WhenBranch(child.getAttribute("test"),
                        parseChildren(child, sqlFragments, includeStack)));
            } else if ("otherwise".equals(child.getTagName())) {
                otherwise = parseChildren(child, sqlFragments, includeStack);
            }
        }
        return new ChooseNode(whens, otherwise);
    }

    private static String emptyToNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }
}
