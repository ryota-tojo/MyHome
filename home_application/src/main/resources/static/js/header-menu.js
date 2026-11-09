document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.site-header').forEach(header => {
    const toggle = header.querySelector('.nav-toggle');
    if (!toggle) return;

    const dropdowns=[...header.querySelectorAll('.menu-dropdown')];
    let submenuCloseTimer;
    const closeSubmenus=(except=null)=>dropdowns.forEach(dropdown=>{
      if(dropdown===except)return;
      dropdown.classList.remove('submenu-open');
      dropdown.querySelector('.menu-trigger')?.setAttribute('aria-expanded','false');
    });

    dropdowns.forEach(dropdown=>{
      const trigger=dropdown.querySelector('.menu-trigger');
      const panel=dropdown.querySelector('.menu-panel');
      if(!trigger||!panel)return;
      trigger.setAttribute('aria-expanded','false');
      trigger.setAttribute('aria-haspopup','true');
      trigger.addEventListener('click',event=>{
        event.preventDefault();
        event.stopPropagation();
        const willOpen=!dropdown.classList.contains('submenu-open');
        closeSubmenus(dropdown);
        dropdown.classList.toggle('submenu-open',willOpen);
        trigger.setAttribute('aria-expanded',String(willOpen));
      });
    });

    const close = () => {
      clearTimeout(submenuCloseTimer);
      header.classList.remove('menu-open');
      document.body.classList.remove('header-menu-open');
      toggle.setAttribute('aria-expanded', 'false');
      // Keep an expanded submenu in the layout until the parent menu has
      // finished shrinking. Removing it immediately makes the header jump.
      submenuCloseTimer=setTimeout(closeSubmenus,430);
    };

    const kanbanPage = document.body.classList.contains('kanban-page');
    const viewportWidth = () => window.visualViewport?.width || document.documentElement.clientWidth;
    const forcedHorizontal = () => false;
    const headerPosition = document.createComment('site-header-position');
    header.parentNode.insertBefore(headerPosition,header);
    let headerViewport=null;
    const togglePosition = document.createComment('nav-toggle-position');
    header.insertBefore(togglePosition, toggle);
    const positionViewportToggle = () => {
      if (!forcedHorizontal()) return;
      header.style.removeProperty('position');
      header.style.removeProperty('top');
      header.style.removeProperty('right');
      header.style.removeProperty('left');
      header.style.removeProperty('width');
      header.style.removeProperty('max-width');
      header.style.removeProperty('transform');
    };
    const syncViewportToggle = () => {
      const active = forcedHorizontal();
      if (active) {
        if(!headerViewport){headerViewport=document.createElement('div');headerViewport.className='kanban-header-viewport';document.body.appendChild(headerViewport)}
        if(header.parentNode!==headerViewport)headerViewport.appendChild(header);
        document.body.classList.add('kanban-header-viewport-active');
        toggle.classList.add('viewport-nav-toggle', 'is-active');
        if (toggle.parentNode !== header) togglePosition.parentNode.insertBefore(toggle, togglePosition.nextSibling);
        positionViewportToggle();
      } else {
        if(header.parentNode!==headerPosition.parentNode)headerPosition.parentNode.insertBefore(header,headerPosition.nextSibling);
        headerViewport?.remove();headerViewport=null;
        document.body.classList.remove('kanban-header-viewport-active');
        header.style.removeProperty('position');
        header.style.removeProperty('top');
        header.style.removeProperty('right');
        header.style.removeProperty('left');
        header.style.removeProperty('width');
        header.style.removeProperty('max-width');
        header.style.removeProperty('transform');
        toggle.classList.remove('viewport-nav-toggle', 'is-active');
        toggle.style.removeProperty('left');
        toggle.style.removeProperty('top');
        toggle.style.removeProperty('right');
        toggle.style.removeProperty('transform');
        if (toggle.parentNode !== header) {
          togglePosition.parentNode.insertBefore(toggle, togglePosition.nextSibling);
        }
      }
    };

    if (kanbanPage) {
      syncViewportToggle();
      window.visualViewport?.addEventListener('resize', () => {
        syncViewportToggle();
        positionViewportToggle();
      }, {passive: true});
    }

    const updateHorizontalMenu = () => {
      const wasHorizontal = header.classList.contains('horizontal-menu');
      header.classList.remove('horizontal-menu');
      const pageOverflows = document.documentElement.scrollWidth > document.documentElement.clientWidth + 1;
      const headerOverflows = header.scrollWidth > header.clientWidth + 1;
      const main = document.querySelector('main');
      const overflowTargets = main ? [main, ...main.querySelectorAll('.kanban-wrap,.table-wrap,[data-horizontal-scroll]')] : [];
      const contentOverflows = overflowTargets.some(element =>
        element.clientWidth > 0 && element.scrollWidth > element.clientWidth + 1
      );
      // Desktop pages keep the full navigation. Overflow detection is only a
      // fallback for responsive widths; kanban explicitly opts into the
      // viewport-following hamburger because its board is intentionally wide.
      const responsiveOverflow = window.innerWidth <= 900 &&
        (pageOverflows || headerOverflows || contentOverflows);
      const horizontal = forcedHorizontal() || responsiveOverflow;
      header.classList.toggle('horizontal-menu', horizontal);
      if (wasHorizontal && !horizontal) close();
    };

    toggle.addEventListener('click', () => {
      if(header.classList.contains('menu-open')){close();return;}
      clearTimeout(submenuCloseTimer);
      header.classList.add('menu-open');
      document.body.classList.add('header-menu-open');
      toggle.setAttribute('aria-expanded', 'true');
    });

    header.querySelectorAll('nav a').forEach(link => link.addEventListener('click', close));
    document.addEventListener('click', event => {
      if (!header.contains(event.target) && !toggle.contains(event.target)) close();
      else if(!toggle.contains(event.target)&&!event.target.closest('.menu-dropdown'))closeSubmenus();
    });
    document.addEventListener('keydown',event=>{if(event.key==='Escape'){closeSubmenus();}});
    window.addEventListener('resize', () => {
      syncViewportToggle();
      if (window.innerWidth > 900 && !header.classList.contains('horizontal-menu')) close();
      updateHorizontalMenu();
      positionViewportToggle();
    });
    window.addEventListener('load', updateHorizontalMenu);
    requestAnimationFrame(updateHorizontalMenu);
    const main = document.querySelector('main');
    if (main && window.ResizeObserver) {
      let resizeFrame;
      new ResizeObserver(() => {
        cancelAnimationFrame(resizeFrame);
        resizeFrame = requestAnimationFrame(updateHorizontalMenu);
      }).observe(main);
    }
  });
});
