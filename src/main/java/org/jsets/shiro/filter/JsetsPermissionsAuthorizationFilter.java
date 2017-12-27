package org.jsets.shiro.filter;

import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.subject.Subject;
/**
 * 重写PermissionsAuthorizationFilter，使其继承自JsetsAuthorizationFilter
 * 
 * @author wangjie (http://www.jianshu.com/u/ffa3cba4c604) 
 * @date 2016年6月24日 下午2:55:15
 */
public class JsetsPermissionsAuthorizationFilter extends JsetsAuthorizationFilter{
	
    public boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) throws IOException {
        Subject subject = getSubject(request, response);
        String[] perms = (String[]) mappedValue;
        boolean isPermitted = true;
        if (perms != null && perms.length > 0) {
            if (perms.length == 1) {
                if (!subject.isPermitted(perms[0])) {
                    isPermitted = false;
                }
            } else {
                if (!subject.isPermittedAll(perms)) {
                    isPermitted = false;
                }
            }
        }
        return isPermitted;
    }
    
}