package com.gionee.gnif3.api;

import com.gionee.gnif3.unitofwork.UnitOfWork;

/**
 * Created by Leon.Yu on 2016/9/29.
 */
public interface CommandHandlerInterceptor<T extends Message<?>> {
    Object handle(UnitOfWork unitOfWork, InterceptorChain interceptorChain);
}
