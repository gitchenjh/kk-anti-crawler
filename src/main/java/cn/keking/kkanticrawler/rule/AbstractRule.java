package cn.keking.kkanticrawler.rule;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author kl @kailing.pub
 * @since 2019/7/8
 */
public abstract class AbstractRule implements KKAntiCrawlerRule {

    @Override
    public boolean execute(HttpServletRequest request, HttpServletResponse response) {
        return doExecute(request,response);
    }

    protected abstract boolean doExecute(HttpServletRequest request, HttpServletResponse response);
}
