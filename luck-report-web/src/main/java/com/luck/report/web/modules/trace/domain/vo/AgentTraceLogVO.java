package com.luck.report.web.modules.trace.domain.vo;

import lombok.Data;

/**
 * 前端 agent 链路日志条目
 * 前端 logger 在 agent run 期间产生的单条日志，随批量上报接口传入
 *
 * @author luck
 */
@Data
public class AgentTraceLogVO {

    /** 链路标识，sessionId:turnId 格式，记录日志时的快照 */
    private String traceId;

    /** 全局递增序号，保证按序输出 */
    private Long seq;

    /** 客户端时间戳（毫秒） */
    private Long timestamp;

    /** 日志级别：info / warn / error */
    private String level;

    /** 模块命名空间，如 dispatcher / check-node */
    private String namespace;

    /** 日志文本 */
    private String message;

    /** 附加数据，前端序列化后的 JSON 字符串，可为空 */
    private String data;
}
