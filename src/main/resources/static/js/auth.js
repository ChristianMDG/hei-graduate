// Shared helper for every Thymeleaf page: stores the JWT issued by POST /auth/login
// and attaches it to every call made against the JSON API under /api/**.
(function (window) {
  const TOKEN_KEY = "hei_token";
  const USER_KEY = "hei_user";

  function setSession(token, user) {
    localStorage.setItem(TOKEN_KEY, token);
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  function getToken() {
    return localStorage.getItem(TOKEN_KEY);
  }

  function getUser() {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  }

  function isAdmin() {
    const user = getUser();
    return !!user && user.role === "ADMIN";
  }

  function logout() {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    window.location.href = "/login";
  }

  function requireAuth() {
    if (!getToken()) {
      window.location.href = "/login";
    }
  }

  // Wraps fetch(): adds the Authorization header, redirects to /login on 401.
  async function authFetch(url, options) {
    const token = getToken();
    const opts = options || {};
    opts.headers = Object.assign({}, opts.headers, {
      Authorization: "Bearer " + token,
    });
    const response = await fetch(url, opts);
    if (response.status === 401) {
      logout();
      throw new Error("Session expirée");
    }
    return response;
  }

  window.heiAuth = {
    setSession,
    getToken,
    getUser,
    isAdmin,
    logout,
    requireAuth,
    authFetch,
  };
})(window);
