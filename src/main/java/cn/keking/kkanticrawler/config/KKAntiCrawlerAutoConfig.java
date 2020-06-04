package cn.keking.kkanticrawler.config;

import cn.keking.kkanticrawler.ValidateFormService;
import cn.keking.kkanticrawler.constant.KKAntiCrawlerConsts;
import cn.keking.kkanticrawler.interceptor.KKAntiCrawlerInterceptor;
import cn.keking.kkanticrawler.rule.IpRule;
import cn.keking.kkanticrawler.rule.KKAntiCrawlerRule;
import cn.keking.kkanticrawler.rule.RuleActuator;
import cn.keking.kkanticrawler.rule.UaRule;
import cn.keking.kkanticrawler.servlet.RefreshFormServlet;
import cn.keking.kkanticrawler.servlet.ValidateFormServlet;
import cn.keking.kkanticrawler.util.VerifyImageUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RedissonAutoConfiguration 的 AutoConfigureOrder 为默认值(0)，此处在它后面加载
 * @author kl @kailing.pub
 * @since 2019/7/8
 */
@Configuration
@EnableConfigurationProperties(KKAntiCrawlerProperties.class)
@ConditionalOnProperty(prefix = "anti.crawler", value = "enabled", havingValue = "true")
@Import({RedissonAutoConfig.class, WebMvcConfig.class})
public class KKAntiCrawlerAutoConfig {

    @Bean
    public ServletRegistrationBean validateFormServlet() {
        return new ServletRegistrationBean(new ValidateFormServlet(), KKAntiCrawlerConsts.VALIDATE_REQUEST_URI);
    }

    @Bean
    public ServletRegistrationBean refreshFormServlet() {
        return new ServletRegistrationBean(new RefreshFormServlet(), KKAntiCrawlerConsts.REFRESH_REQUEST_URI);
    }

    @Bean
    @ConditionalOnProperty(prefix = "anti.crawler.ip-rule",value = "enabled", havingValue = "true", matchIfMissing = true)
    public IpRule ipRule(){
        return new IpRule();
    }

    @Bean
    @ConditionalOnProperty(prefix = "anti.crawler.ua-rule",value = "enabled", havingValue = "true", matchIfMissing = true)
    public UaRule uaRule() {
        return new UaRule();
    }

    @Bean
    public VerifyImageUtil verifyImageUtil() {
        return new VerifyImageUtil();
    }

    @Bean
    public RuleActuator ruleActuator(final List<KKAntiCrawlerRule> rules){
        final List<KKAntiCrawlerRule> antiCrawlerRules = rules.stream().sorted(Comparator.comparingInt(KKAntiCrawlerRule::getOrder)).collect(Collectors.toList());
        return new RuleActuator(antiCrawlerRules);
    }

    @Bean
    public ValidateFormService validateFormService(RuleActuator ruleActuator, VerifyImageUtil verifyImageUtil){
        return new ValidateFormService(ruleActuator, verifyImageUtil);
    }

    @Bean
    public KKAntiCrawlerInterceptor antiCrawlerInterceptor() {
        return new KKAntiCrawlerInterceptor();
    }

}
