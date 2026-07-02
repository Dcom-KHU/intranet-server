package com.dcom.intranet.mypage;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyWrittenCommentReaderConfig {

    @Bean
    @ConditionalOnMissingBean(MyWrittenCommentReader.class)
    public MyWrittenCommentReader emptyMyWrittenCommentReader() {
        return new EmptyMyWrittenCommentReader();
    }
}
