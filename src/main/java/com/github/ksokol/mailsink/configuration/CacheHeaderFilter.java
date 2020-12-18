package com.github.ksokol.mailsink.configuration;

import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.springframework.http.HttpHeaders.CACHE_CONTROL;
import static org.springframework.http.HttpHeaders.EXPIRES;
import static org.springframework.http.HttpHeaders.PRAGMA;

/**
 * @author Kamill Sokol
 */
@Component
public class CacheHeaderFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletRsponse = (HttpServletResponse) response;

        httpServletRsponse.setHeader(PRAGMA, "no-cache");
        httpServletRsponse.setHeader(EXPIRES, "-1");
        httpServletRsponse.setHeader(CACHE_CONTROL, "no-store");

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {}
}
