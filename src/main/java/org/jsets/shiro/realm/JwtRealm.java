package org.jsets.shiro.realm;

import java.util.Set;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jsets.shiro.config.ShiroProperties;
import org.jsets.shiro.model.StatelessAccount;
import org.jsets.shiro.service.ShiroCryptoService;
import org.jsets.shiro.token.JwtToken;
import org.jsets.shiro.util.Commons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.jsonwebtoken.JwtException;
/**
 * 基于JWT（ JSON WEB TOKEN）的控制域
 * @author wangjie (http://www.jianshu.com/u/ffa3cba4c604) 
 * @date 2016年6月24日 下午2:55:15
 */
public class JwtRealm extends AuthorizingRealm{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(JwtRealm.class);
	
	private final ShiroCryptoService cryptoService;

	public JwtRealm(ShiroCryptoService cryptoService){
		this.cryptoService = cryptoService;
	}

	public Class<?> getAuthenticationTokenClass() {
		return JwtToken.class;
	}
	
	/**
	 *  认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		System.out.println("jwt 认证");
		JwtToken jwtToken = (JwtToken) token;
		String jwt = (String) jwtToken.getPrincipal();
		StatelessAccount statelessAccount;
		try{
			statelessAccount = this.cryptoService.parseJwt(jwt);
		} catch(JwtException e){
			LOGGER.warn(e.getMessage(),e);
			throw new AuthenticationException(ShiroProperties.MSG_JWT_AUTHC_ERROR);
		} catch(IllegalArgumentException e){
			LOGGER.warn(e.getMessage(),e);
			throw new AuthenticationException(ShiroProperties.MSG_JWT_AUTHC_ERROR);
		} 
		if(null == statelessAccount){
			throw new AuthenticationException(ShiroProperties.MSG_JWT_AUTHC_ERROR);
		}
		// 如果要使token只能使用一次，此处可以过滤并缓存jwtPlayload.getId()
		// 可以做签发方验证
		// 可以做接收方验证
        return new SimpleAuthenticationInfo(statelessAccount,Boolean.TRUE,getName());
	}
	
	/** 
     * 授权 
     */  
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		Object principal = principals.getPrimaryPrincipal();
		if(!(principal instanceof StatelessAccount)) return null;
		SimpleAuthorizationInfo info =  new SimpleAuthorizationInfo();
		StatelessAccount statelessAccount = (StatelessAccount)principal;
		Set<String> roles = Commons.split(statelessAccount.getRoles());
		Set<String> permissions = Commons.split(statelessAccount.getPerms());
		if(null!=roles&&!roles.isEmpty())
			info.setRoles(roles);
		if(null!=permissions&&!permissions.isEmpty())
			info.setStringPermissions(permissions);
        return info;  
	}
}