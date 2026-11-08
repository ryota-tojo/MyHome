(() => {
  const EYE = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M2.2 12s3.5-6 9.8-6 9.8 6 9.8 6-3.5 6-9.8 6-9.8-6-9.8-6Zm9.8 3.3a3.3 3.3 0 1 0 0-6.6 3.3 3.3 0 0 0 0 6.6Z"/></svg>';
  const EYE_OFF = '<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m3.3 2 18.7 18.7-1.3 1.3-3.1-3.1A11 11 0 0 1 12 20C5.7 20 2.2 14 2.2 14a17 17 0 0 1 3.5-4.2L2 6.1 3.3 4.8Zm4.6 10a4.1 4.1 0 0 0 5.4 5.4L7.9 12Zm4.1-6c6.3 0 9.8 6 9.8 6a17 17 0 0 1-2.8 3.5l-2.2-2.2a4.8 4.8 0 0 0-6.1-6.1L9.5 6.1A12 12 0 0 1 12 6Z"/></svg>';
  function enhance(input) {
    if (input.dataset.passwordToggle === 'true') return;
    input.dataset.passwordToggle = 'true';
    const wrapper = document.createElement('span');
    wrapper.className = 'password-field';
    input.parentNode.insertBefore(wrapper, input);
    wrapper.appendChild(input);
    const button = document.createElement('button');
    button.type = 'button';
    button.className = 'password-toggle';
    button.setAttribute('aria-label', 'パスワードを表示');
    button.setAttribute('aria-pressed', 'false');
    button.innerHTML = EYE;
    button.addEventListener('click', () => {
      const show = input.type === 'password';
      input.type = show ? 'text' : 'password';
      button.setAttribute('aria-label', show ? 'パスワードを非表示' : 'パスワードを表示');
      button.setAttribute('aria-pressed', String(show));
      button.innerHTML = show ? EYE_OFF : EYE;
      input.focus({preventScroll:true});
    });
    wrapper.appendChild(button);
  }
  function apply(root = document) { root.querySelectorAll?.('input[type="password"]:not([data-password-toggle="true"])').forEach(enhance); }
  const start = () => {
    if (!document.getElementById('password-toggle-style')) {
      const style = document.createElement('link');
      style.id = 'password-toggle-style';
      style.rel = 'stylesheet';
      style.href = '/css/password-toggle.css';
      document.head.appendChild(style);
    }
    apply();
    new MutationObserver(records => records.forEach(record => record.addedNodes.forEach(node => {
      if (node.nodeType === Node.ELEMENT_NODE) apply(node);
    }))).observe(document.body, {childList:true, subtree:true});
  };
  document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', start) : start();
})();
