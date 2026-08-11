/**
 * DASHBOARD LAYOUT - JavaScript
 * Handles sidebar toggle, responsive behavior, and interactions
 */

document.addEventListener("DOMContentLoaded", function () {
    initSidebarToggle();
    initUserMenuToggle();
    handleResponsiveBehavior();
    setActiveMenuItem();
    preventBodyScroll();
});

/**
 * Initialize sidebar toggle functionality
 */
function initSidebarToggle() {
    // Como os botões podem ser renderizados dinamicamente ou via Facelets, usamos delegação ou busca global
    document.addEventListener("click", function (e) {
        const toggleButton = e.target.closest('.sidebar-toggle-button');
        if (toggleButton) {
            e.preventDefault();
            e.stopPropagation();
            toggleSidebar();
        }
    });

    // Fecha a sidebar ao pressionar Escape
    document.addEventListener("keydown", function (e) {
        const layout = document.querySelector('.dashbord-layout');
        if (e.key === "Escape" && layout && layout.classList.contains('sidebar-visible')) {
            closeSidebar();
        }
    });
}

/**
 * Global Document Click Listener - Fecha a sidebar ao clicar fora dela em dispositivos móveis
 */
document.addEventListener('click', function (event) {
    const layout = document.querySelector('.dashbord-layout');
    const sidebar = document.querySelector('.layout-side-bar-container');
    
    // Verifica se estamos em tela mobile (onde a sidebar fica oculta por padrão fora da tela)
    const isMobile = window.innerWidth < 992;

    if (isMobile && layout && layout.classList.contains('sidebar-visible')) {
        const isClickInsideSidebar = sidebar ? sidebar.contains(event.target) : false;
        const isClickOnToggler = event.target.closest('.sidebar-toggle-button');

        // Se o clique NÃO foi dentro da sidebar e NÃO foi no botão de alternar, fecha o menu
        if (!isClickInsideSidebar && !isClickOnToggler) {
            closeSidebar();
        }
    }
});

/**
 * Toggle sidebar visibility (Alterna a classe no layout pai)
 */
function toggleSidebar() {
    const layout = document.querySelector('.dashbord-layout');
    if (layout) {
        layout.classList.toggle('sidebar-visible');
        
        // Bloqueia o scroll do body em telas pequenas quando o menu estiver aberto
        if (window.innerWidth < 992) {
            document.body.style.overflow = layout.classList.contains('sidebar-visible') ? "hidden" : "";
        }
    }
}

/**
 * Close sidebar explicitly
 */
function closeSidebar() {
    const layout = document.querySelector('.dashbord-layout');
    if (layout && layout.classList.contains('sidebar-visible')) {
        layout.classList.remove('sidebar-visible');
        document.body.style.overflow = "";
    }
}

/**
 * Initialize user menu dropdown toggle
 */
function initUserMenuToggle() {
    document.addEventListener("click", function (e) {
        const trigger = e.target.closest(".sidebar-user-trigger");
        
        if (trigger) {
            e.preventDefault();
            e.stopPropagation();

            const userCard = trigger.closest(".sidebar-user-card");
            if (userCard) {
                userCard.classList.toggle("open");
                const dropdown = userCard.querySelector(".sidebar-user-dropdown");
                if (dropdown) {
                    dropdown.classList.toggle("d-none");
                }
            }
        } else {
            // Fecha o dropdown do usuário ao clicar fora dele
            const userCards = document.querySelectorAll(".sidebar-user-card");
            userCards.forEach((card) => {
                if (!card.contains(e.target)) {
                    card.classList.remove("open");
                    const dropdown = card.querySelector(".sidebar-user-dropdown");
                    if (dropdown) {
                        dropdown.classList.add("d-none");
                    }
                }
            });
        }
    });
}

/**
 * Handle responsive behavior for sidebar
 */
function handleResponsiveBehavior() {
    window.addEventListener("resize", function () {
        const isLargeScreen = window.innerWidth >= 992;
        const layout = document.querySelector('.dashbord-layout');

        if (isLargeScreen) {
            // Remove overflow hidden do body caso venha do mobile
            document.body.style.overflow = "";
            if (layout && layout.classList.contains('sidebar-visible')) {
                layout.classList.remove('sidebar-visible');
            }
        }
    });
}

/**
 * Smooth scroll to top
 */
function scrollToTop() {
    window.scrollTo({
        top: 0,
        behavior: "smooth",
    });
}

/**
 * Add active class to current menu item based on current URL
 */
function setActiveMenuItem() {
    const currentPage = window.location.pathname;
    const menuItems = document.querySelectorAll(".ui-menuitem a, .ui-menuitem-link");

    menuItems.forEach((item) => {
        const href = item.getAttribute("href") || item.getAttribute("outcome");
        if (href && currentPage.includes(href)) {
            item.classList.add("ui-state-active");
            const parentMenu = item.closest(".ui-menuitem");
            if (parentMenu) {
                parentMenu.classList.add("ui-state-active");
            }
        }
    });
}

/**
 * Prevent sidebar from scrolling body if needed
 */
function preventBodyScroll() {
    const sidebar = document.querySelector(".layout-side-bar-container");

    if (sidebar) {
        sidebar.addEventListener("wheel", function (e) {
            if (sidebar.scrollHeight > sidebar.clientHeight) {
                return;
            }
            e.preventDefault();
        }, { passive: true });
    }
}