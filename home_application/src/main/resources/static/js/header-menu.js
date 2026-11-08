document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.site-header').forEach(header => {
    const toggle = header.querySelector('.nav-toggle');
    if (!toggle) return;

    const dropdowns=[...header.querySelectorAll('.menu-dropdown')];
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
      header.classList.remove('menu-open');
      toggle.setAttribute('aria-expanded', 'false');
      closeSubmenus();
    };

    toggle.addEventListener('click', () => {
      const open = header.classList.toggle('menu-open');
      toggle.setAttribute('aria-expanded', String(open));
    });

    header.querySelectorAll('nav a').forEach(link => link.addEventListener('click', close));
    document.addEventListener('click', event => {
      if (!header.contains(event.target)) close();
      else if(!event.target.closest('.menu-dropdown'))closeSubmenus();
    });
    document.addEventListener('keydown',event=>{if(event.key==='Escape'){closeSubmenus();}});
    window.addEventListener('resize', () => {
      if (window.innerWidth > 900) close();
    });
  });
});
