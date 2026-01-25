package com.vv.cloudfarming.order.mq.modal;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;

import java.util.List;

/**
 * 延迟消息
 */
@Data
public class MultiDelayMessage<T> {

    /**
     * 消息体
     */
    private T data;

    /**
     * 记录延迟时间的集合
     */
    private List<Integer> delayMillis;

    public MultiDelayMessage() {}

    public MultiDelayMessage(T data, List<Integer> delayMillis) {
        this.data = data;
        this.delayMillis = delayMillis;
    }
    public static <T> MultiDelayMessage<T> of(T data, Integer ... delayMillis){
        return new MultiDelayMessage<>(data, CollUtil.newArrayList(delayMillis));
    }

    /**
     * 获取并移除下一个延迟时间
     * @return 队列中的第一个延迟时间
     */
    public Integer removeNextDelay(){
        return delayMillis.remove(0);
    }

    /**
     * 是否还有下一个延迟时间
     */
    public boolean hasNextDelay(){
        return !delayMillis.isEmpty();
    }
}
