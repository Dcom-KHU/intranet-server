package com.dcom.intranet.mypage.config;

import com.dcom.intranet.mypage.service.EmptyMyWrittenPostReader;
import com.dcom.intranet.mypage.service.MyWrittenPostReader;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyWrittenPostReaderConfig {

    @Bean
    @ConditionalOnMissingBean(MyWrittenPostReader.class)
    public MyWrittenPostReader emptyMyWrittenPostReader() {
        return new EmptyMyWrittenPostReader();
    }
}
