package com.dingzk.codeless.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.dingzk.codeless.model.entity.App;
import com.dingzk.codeless.mapper.AppMapper;
import com.dingzk.codeless.service.AppService;
import org.springframework.stereotype.Service;

/**
 *  服务层实现。
 *
 * @author dingzk
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App>  implements AppService{

}
