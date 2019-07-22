package de.bwl.bwfla.common.services.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import de.bwl.bwfla.common.exceptions.BWFLAException;
import org.apache.tamaya.Configuration;
import org.apache.tamaya.ConfigurationProvider;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class ServletAuthenticationFilter  implements Filter {

    private ServletContext context;

    private static final java.util.logging.Logger LOG = Logger.getLogger( ServletAuthenticationFilter.class.getName() );

    private static final String AUTH_HEADER_KEY = "Authorization";
    private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer "; // with trailing space to separate token

    private static final int STATUS_CODE_UNAUTHORIZED = 401;

    private String apiSecret = MachineTokenProvider.getApiSecret();
    private List<String> excludeUrls;
    private List<String> excludePaths;

    @Override
    public void init( FilterConfig filterConfig ) throws ServletException {
        LOG.info( "JwtAuthenticationFilter initialized" );

        final Configuration config = ConfigurationProvider.getConfiguration();

        String excludePattern = filterConfig.getInitParameter("excludedUrls");
        if(excludePattern == null)
            excludeUrls = new ArrayList<>();
        else
            excludeUrls = Arrays.asList(excludePattern.split(","));

        String excludePathsParam = filterConfig.getInitParameter("excludePaths");
        if(excludePathsParam == null)
            excludePaths = new ArrayList<>();
        else
            excludePaths = Arrays.asList(excludePathsParam.split(","));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        boolean excludePath = false;
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse response=(HttpServletResponse) servletResponse;

        String path = httpRequest.getServletPath();
        if(httpRequest.getQueryString() != null && !httpRequest.getQueryString().isEmpty())
            path += "?" + httpRequest.getQueryString();

        if(excludeUrls.contains(path))
            excludePath = true;

        String pathInfo = httpRequest.getPathInfo();
        if(pathInfo != null && !pathInfo.isEmpty()) {
            for(String p : excludePaths) {
                if (pathInfo.startsWith(p))
                    excludePath = true;
            }
        }

        if(apiSecret != null && !excludePath) {
            String jwt = getBearerToken(httpRequest);
            // System.out.println("apiSecret " + apiSecret + " jwt " + jwt + " ");

            try{
                validateToken(jwt);
            }
            catch (Exception e)
            {
                // e.printStackTrace();
                System.out.println("not authenticated " + httpRequest.getContextPath() + " " + httpRequest.getPathInfo() + " " + httpRequest.getServletPath());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }
        filterChain.doFilter( servletRequest, servletResponse );
    }

    private void validateToken(String token) throws Exception {

        if(token == null)
            throw new BWFLAException("no token found");

        try {
            Algorithm algorithm = Algorithm.HMAC256(apiSecret);
            JWTVerifier verifier = JWT.require(algorithm)
                    .build(); //Reusable verifier instance
            DecodedJWT jwt = verifier.verify(token);
        } catch (JWTVerificationException exception){
            exception.printStackTrace();
            throw exception;
        }
    }

    private String getBearerToken( HttpServletRequest request ) {
        String authHeader = request.getHeader(AUTH_HEADER_KEY);
        if (authHeader != null && authHeader.startsWith(AUTH_HEADER_VALUE_PREFIX)) {
            return authHeader.substring(AUTH_HEADER_VALUE_PREFIX.length());
        }
        String query = request.getQueryString();

        return null;
    }
}
