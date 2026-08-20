
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

  function getRole() {
    const user = getUser();
    return user ? user.role : null;
  }

  function isAdmin() {
    return getRole() === "ADMIN";
  }

  function isTeacher() {
    return getRole() === "TEACHER";
  }

  function isStudent() {
    return getRole() === "STUDENT";
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

  /**
   * Construit le menu latéral selon le rôle (CDC §6).
   * - STUDENT : Mes notes, Mes relevés
   * - TEACHER : Mes cours / Saisie notes (pages notes), Promotions en lecture si besoin
   * - ADMIN   : Promotions (gestion diplômés), + accès notes/relevés si utile
   */
  function renderSidebar(activePage) {
    const nav = document.querySelector(".sidebar .nav");
    if (!nav) return;

    const role = getRole();
    let items = [];

    if (role === "STUDENT") {
      items = [
        { href: "/notes", icon: "📊", label: "Mes notes", page: "notes" },
        { href: "/releves", icon: "📄", label: "Mes relevés", page: "releves" },
      ];
    } else if (role === "TEACHER") {
      items = [
        { href: "/notes", icon: "📊", label: "Saisie des notes", page: "notes" },
        { href: "/promotions", icon: "🎓", label: "Promotions", page: "promotions" },
      ];
    } else {
      // ADMIN (et fallback)
      items = [
        { href: "/promotions", icon: "🎓", label: "Promotions", page: "promotions" },
        { href: "/notes", icon: "📊", label: "Notes", page: "notes" },
        { href: "/releves", icon: "📄", label: "Relevés", page: "releves" },
      ];
    }

    nav.innerHTML = items
      .map(function (item) {
        const active = item.page === activePage ? " active" : "";
        return (
          '<a href="' +
          item.href +
          '" class="' +
          active.trim() +
          '">' +
          item.icon +
          " &nbsp; " +
          item.label +
          "</a>"
        );
      })
      .join("");
  }

  window.heiAuth = {
    setSession,
    getToken,
    getUser,
    getRole,
    isAdmin,
    isTeacher,
    isStudent,
    logout,
    requireAuth,
    authFetch,
    renderSidebar,
  };
})(window);
