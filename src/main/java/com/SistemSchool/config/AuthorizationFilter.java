package com.SistemSchool.config;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.SistemSchool.dto.UserDTO;
import com.SistemSchool.io.Perfil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Este Filter corre FORA do ciclo de vida do JSF e é instanciado uma única
 * vez pelo container Servlet, sendo reutilizado para todos os pedidos de
 * todos os utilizadores. Por isso NUNCA deve depender de @Inject de beans
 * CDI/JSF (como SessionBean) nem de FacesContext — em vez disso, lê
 * diretamente o mesmo atributo que o SessionBean escreve na HttpSession
 * real ("loggedUser"). Isto garante isolamento correto por dispositivo/
 * browser, sem qualquer ambiguidade de resolução de scope.
 */
@WebFilter("/*")
public class AuthorizationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthorizationFilter.class);
    private static final String ATTR_LOGGED_USER = "loggedUser";

    // Prefixo de rota -> regra de acesso (avaliada sobre o perfil do utilizador logado)
    private static final Map<String, Predicate<Perfil>> MODULE_RULES = new LinkedHashMap<>();

    static {
        MODULE_RULES.put("/settings/", perfil -> perfil == Perfil.ADMIN);
        MODULE_RULES.put("/management/secretaria/", perfil -> perfil == Perfil.ADMIN || perfil == Perfil.SECRETARY);
        MODULE_RULES.put("/management/financeiro/", perfil -> perfil == Perfil.ADMIN || perfil == Perfil.FINANCIAL);
        MODULE_RULES.put("/management/pedagogico/", perfil -> perfil == Perfil.ADMIN || perfil == Perfil.PEDAGOGICAL);
    }

    private static final String[] PUBLIC_ROUTES = {
            "/login.xhtml",
            "/sign_in.xhtml",
            "/access-denied.xhtml"
    };

    private static final String[] PUBLIC_PREFIXES = {
            "/resources/",
            "/javax.faces.resource/",
            "/jakarta.faces.resource/"
    };

    @Override
    public void init(FilterConfig filterConfig) {
        // nada a inicializar
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        String contextPath = request.getContextPath();
        String path = request.getRequestURI().substring(contextPath.length());

        if (isPublicRoute(path)) {
            chain.doFilter(req, res);
            return;
        }

        HttpSession session = request.getSession(false);

        if (session == null) {
            logger.warn("Acesso negado (sem sessão HTTP) a {}", path);
            response.sendRedirect(contextPath + "/login.xhtml");
            return;
        }

        UserDTO.UserResponseDTO loggedUser = (UserDTO.UserResponseDTO) session.getAttribute(ATTR_LOGGED_USER);

        if (loggedUser == null) {
            logger.warn("Acesso negado (não autenticado) na sessão [{}] a {}", session.getId(), path);
            response.sendRedirect(contextPath + "/login.xhtml");
            return;
        }

        Perfil perfil = loggedUser.getPerfil();

        // ADMIN tem acesso irrestrito a todos os módulos
        if (perfil == Perfil.ADMIN) {
            chain.doFilter(req, res);
            return;
        }

        for (Map.Entry<String, Predicate<Perfil>> rule : MODULE_RULES.entrySet()) {
            if (path.startsWith(rule.getKey()) && !rule.getValue().test(perfil)) {
                logger.warn("Acesso negado ao módulo {} para sessão [{}] com perfil {}",
                        rule.getKey(), session.getId(), perfil);
                response.sendRedirect(contextPath + "/access-denied.xhtml");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private boolean isPublicRoute(String path) {
        for (String route : PUBLIC_ROUTES) {
            if (path.equals(route)) {
                return true;
            }
        }
        for (String prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void destroy() {
        // nada a limpar
    }
}