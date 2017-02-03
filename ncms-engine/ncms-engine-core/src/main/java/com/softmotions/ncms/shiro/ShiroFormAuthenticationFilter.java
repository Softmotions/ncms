package com.softmotions.ncms.shiro;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * @author Adamansky Anton (adamansky@softmotions.com)
 */
public class ShiroFormAuthenticationFilter extends FormAuthenticationFilter {

    /**
     * This authentication filter can be configured with a list of HTTP methods to which it should apply. This
     * method ensures that authentication is <em>only</em> required for those HTTP methods specified. For example,
     * if you had the configuration:
     * <pre>
     *    [urls]
     *    /basic/** = authcNcms[POST,PUT,DELETE]
     * </pre>
     * then a GET request would not required authentication but a POST would.
     *
     * @param request     The current HTTP servlet request.
     * @param response    The current HTTP servlet response.
     * @param mappedValue The array of configured HTTP methods as strings. This is empty if no methods are configured.
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        if (isLoginSubmission(request, response)) {
            return super.isAccessAllowed(request, response, mappedValue);
        }
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String httpMethod = httpRequest.getMethod();
        // Check whether the current request's method requires authentication.
        // If no methods have been configured, then all of them require auth,
        // otherwise only the declared ones need authentication.
        Set<String> methods = httpMethodsFromOptions((String[]) mappedValue);
        boolean authcRequired = methods.isEmpty();
        for (String m : methods) {
            if (httpMethod.toUpperCase(Locale.ENGLISH).equals(m)) { // list of methods is in upper case
                authcRequired = true;
                break;
            }
        }
        if (authcRequired) {
            return super.isAccessAllowed(request, response, mappedValue);
        } else {
            return true;
        }
    }

    private Set<String> httpMethodsFromOptions(String[] options) {
        Set<String> methods = new HashSet<String>();

        if (options != null) {
            for (String option : options) {
                // to be backwards compatible with 1.3, we can ONLY check for known args
                // ideally we would just validate HTTP methods, but someone could already be using this for webdav
                if (!option.equalsIgnoreCase(PERMISSIVE)) {
                    methods.add(option.toUpperCase(Locale.ENGLISH));
                }
            }
        }
        return methods;
    }

}
