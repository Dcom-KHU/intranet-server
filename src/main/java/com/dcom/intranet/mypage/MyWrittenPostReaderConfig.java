package com.dcom.intranet.mypage;

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
