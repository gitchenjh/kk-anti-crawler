package cn.keking.kkanticrawler.config;

import cn.keking.kkanticrawler.interceptor.KKAntiCrawlerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author chenjh
 * @since 2020/2/4 17:40
 */
@Configuration
public class WebMvcConfig extends WebMvcConfigurerAdapter {

    private final KKAntiCrawlerInterceptor kkAntiCrawlerInterceptor;

    public WebMvcConfig(KKAntiCrawlerInterceptor kkAntiCrawlerInterceptor) {
        this.kkAntiCrawlerInterceptor = kkAntiCrawlerInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.kkAntiCrawlerInterceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
