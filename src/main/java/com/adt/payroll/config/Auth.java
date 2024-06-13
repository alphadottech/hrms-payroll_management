package com.adt.payroll.config;

import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.adt.payroll.service.TableDataExtractor;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class Auth {
	String AUTHORITIES_CLAIM = "authorities";

	@Autowired
	private HttpServletRequest request;
	
	@Autowired
	private TableDataExtractor dataExtractor;
	
	@Value("${app.jwt.secret}")
	public String screteKey;
	
	public static String empId;
	
	public static String apiConstant;

	public boolean allow(String apiName, Map<String, String> resourceAttributes) {
		String token = getToken(request);
		Claims claims = getUserIdFromJWT(token);
		List<GrantedAuthority> authorities = getAuthorities(claims);	
		boolean isValidAuthority = false;
		String authority=null;
		String sql = "SELECT r.role_name FROM user_schema.role r JOIN av_schema.api_mapping  am ON r.role_id = am.role_id JOIN av_schema.api_details ad ON am.api_id = ad.api_id WHERE ad.api_name ="
				+ "'" + apiName + "'";
		List<Map<String, Object>> roleeData = dataExtractor.extractDataFromTable(sql);
		for (Map<String, Object> role : roleeData) {
			authority = String.valueOf(role.get("role_name"));
			isValidAuthority = checkAuthority(authority, authorities);
			if (isValidAuthority) {
				break;
			}
		}
		boolean isValidResourse = checkResourse(resourceAttributes, claims);
		return isValidResourse && isValidAuthority;
	}

	public boolean allow(String apiName) {
		apiConstant=apiName;
		String token = getToken(request);
		if (token.isEmpty() || token == null)
			token = getTokenByUrl(request);		
		Claims claims = getUserIdFromJWT(token);
		List<GrantedAuthority> authorities = getAuthorities(claims);
		boolean isValidAuthority = false;
		String authority=null;
		String sql = "SELECT r.role_name FROM user_schema.role r JOIN av_schema.api_mapping  am ON r.role_id = am.role_id JOIN av_schema.api_details ad ON am.api_id = ad.api_id WHERE ad.api_name ="+"'"+apiName+"'";
		List<Map<String, Object>> roleData = dataExtractor.extractDataFromTable(sql);
		for (Map<String, Object> role : roleData) {
	    	 authority = String.valueOf(role.get("role_name"));
	    	 isValidAuthority = checkAuthority(authority, authorities);
	    	 if (isValidAuthority) {
					break;
				}
		}
		return isValidAuthority;
	}

	private boolean checkResourse(Map<String, String> resourceAttributes, Claims claims) {
		boolean isValidResourse = true;
		String employeeId = String.valueOf(claims.get("id"));
		if (resourceAttributes != null) {
			if (resourceAttributes.containsKey("currentUser")) {
				String currentUserId = resourceAttributes.get("currentUser");
				currentUserId = currentUserId != null ? currentUserId.trim() : "";
				isValidResourse = (currentUserId.equalsIgnoreCase(employeeId));
			}
		}
		return isValidResourse;
	}

	private boolean checkAuthority(String authority, List<GrantedAuthority> authorities) {
		return authorities.stream().anyMatch(s -> {
			s = (SimpleGrantedAuthority) s;
			return s.getAuthority().equalsIgnoreCase(authority);
		});
	}

	private List<GrantedAuthority> getAuthorities(Claims claims) {
		String employeeId = String.valueOf(claims.get("id"));
		empId = employeeId;
		List<GrantedAuthority> authorities = Arrays.stream(claims.get(AUTHORITIES_CLAIM).toString().split(","))
				.map(SimpleGrantedAuthority::new).collect(Collectors.toList());
		return authorities;
	}

	private String getToken(HttpServletRequest request) {
		String token = request.getHeader("authorization");
		token = token != null ? token.trim() : "";
		token = token.replace("Bearer", "");
		token = token != null ? token.trim() : "";
		return token;
	}

	@SuppressWarnings("rawtypes")
	public Claims getUserIdFromJWT(String token) {
		int i = token.lastIndexOf('.');
		String withoutSignature = token.substring(0, i + 1);
		Jwt<Header, Claims> untrusted = Jwts.parser().parseClaimsJwt(withoutSignature);
		return untrusted.getBody();
	}
	
	private String getTokenByUrl(HttpServletRequest request) {
		String token = request.getQueryString();
		token = token.replace("Authorization=", "");
		return token;
	}
	
	public String getEmail() {
		String sql = "select email from user_schema._employee where employee_id=" +empId;
		List<Map<String, Object>> empData = dataExtractor.extractDataFromTable(sql);
		if (empData != null || !empData.isEmpty()) {
			Map<String, Object> firstMap = empData.get(0);
			return String.valueOf(firstMap.get("email"));
		}
		return "email id not present";
	}
	
	public String tokenGanreate(String emailId) {
		String sql = "SELECT r.role_name, e.employee_id FROM user_schema.role r JOIN user_schema.user_authority ua ON r.role_id = ua.role_id JOIN user_schema._employee e ON ua.employee_id = e.employee_id WHERE e.email ="
				+ "'" + emailId + "'";
		List<Map<String, Object>> roleData = dataExtractor.extractDataFromTable(sql);
		if (roleData != null) {
			String roleName = "";
			String result = null;
			String empId = null;
			int i = 0;
			for (Map<String, Object> role : roleData) {
				i++;
				if (i > 1) {
					roleName = roleName + ",";
				}
				roleName = roleName + String.valueOf(role.get("role_name"));
				empId = String.valueOf(role.get("employee_id"));
			}

			long millisecondsInFiveDays = TimeUnit.DAYS.toMillis(5);
			String newJwtToken = Jwts.builder().setId(UUID.randomUUID().toString()).setSubject(emailId)
					.setIssuedAt(Date.from(Instant.now()))
					.setExpiration(new Date(System.currentTimeMillis() + millisecondsInFiveDays)).claim("id", empId)
					.claim(AUTHORITIES_CLAIM, roleName).signWith(SignatureAlgorithm.HS512, screteKey).compact();
			return newJwtToken;
		}

		return null;
	}
}
