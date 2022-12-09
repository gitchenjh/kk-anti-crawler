package cn.keking.kkanticrawler.interceptor;

import cn.keking.kkanticrawler.annotation.KKAntiCrawler;
import cn.keking.kkanticrawler.config.KKAntiCrawlerProperties;
import cn.keking.kkanticrawler.module.VerifyImageDTO;
import cn.keking.kkanticrawler.rule.RuleActuator;
import cn.keking.kkanticrawler.util.CorsUtil;
import cn.keking.kkanticrawler.util.VerifyImageUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

/**
 * @author chenjh
 * @since 2020/2/4 17:45
 */
public class KKAntiCrawlerInterceptor extends HandlerInterceptorAdapter {


    private String antiCrawlerForm;

    private RuleActuator actuator;

    private List<String> includeUrls;

    private boolean globalFilterMode;

    private VerifyImageUtil verifyImageUtil;

    private final AtomicBoolean initialized = new AtomicBoolean(false);

    public void init(ServletContext context) {
        ClassPathResource classPathResource = new ClassPathResource("verify/index.html");
        try {
            classPathResource.getInputStream();
            byte[] bytes = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
            this.antiCrawlerForm = new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("反爬虫验证模板加载失败！");
            e.printStackTrace();
        }
        ApplicationContext ctx = WebApplicationContextUtils.getWebApplicationContext(context);
        assert ctx != null;
        this.actuator = ctx.getBean(RuleActuator.class);
        this.verifyImageUtil = ctx.getBean(VerifyImageUtil.class);
        this.includeUrls = ctx.getBean(KKAntiCrawlerProperties.class).getIncludeUrls();
        this.globalFilterMode = ctx.getBean(KKAntiCrawlerProperties.class).isGlobalFilterMode();
        if (this.includeUrls == null) {
            this.includeUrls = new ArrayList<>();
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!initialized.get()) {
            init(request.getServletContext());
            initialized.set(true);
        }
        HandlerMethod handlerMethod;
        try {
            handlerMethod = (HandlerMethod) handler;
        } catch (ClassCastException e) {
            return true;
        }
        Method method = handlerMethod.getMethod();
        KKAntiCrawler kkAntiCrawler = AnnotationUtils.findAnnotation(method, KKAntiCrawler.class);
        boolean isKKAntiCrawlerAnnotation = kkAntiCrawler != null;
        String requestUrl = request.getRequestURI();
        if (isIntercept(requestUrl, isKKAntiCrawlerAnnotation) && !actuator.isAllowed(request, response)) {
            CorsUtil.setCrosHeader(response);
            response.setContentType("text/html;charset=utf-8");
            response.setStatus(509);
            VerifyImageDTO verifyImage = verifyImageUtil.generateVerifyImg();
            verifyImageUtil.saveVerifyCodeToRedis(verifyImage);
            String str1 = this.antiCrawlerForm.replace("verifyId_value", verifyImage.getVerifyId());
            String str2 = str1.replaceAll("verifyImg_value", verifyImage.getVerifyImgStr());
            String str3 = str2.replaceAll("realRequestUri_value", requestUrl);
            response.getWriter().write(str3);
            response.getWriter().close();
            return false;
        }
        return true;
    }

    /**
     * 是否拦截
     * @param requestUrl 请求uri
     * @param isAntiCrawlerAnnotation 是否有AntiCrawler注解
     * @return 是否拦截
     */
    public boolean isIntercept(String requestUrl, Boolean isAntiCrawlerAnnotation) {
        if (this.globalFilterMode || isAntiCrawlerAnnotation || this.includeUrls.contains(requestUrl)) {
            return true;
        } else {
            for (String includeUrl : includeUrls) {
                if (Pattern.matches(includeUrl, requestUrl)) {
                    return true;
                }
            }
            return false;
        }
    }
}
