(() => {
  const lockForm = form => {
    form.dataset.submitLocked = 'true';
    form.setAttribute('aria-busy', 'true');
    form.querySelectorAll('button[type="submit"], button:not([type]), input[type="submit"]')
      .forEach(control => {
        control.classList.add('submit-locked');
        control.setAttribute('aria-disabled', 'true');
      });
  };

  const unlockForm = form => {
    delete form.dataset.submitLocked;
    form.removeAttribute('aria-busy');
    form.querySelectorAll('.submit-locked').forEach(control => {
      control.classList.remove('submit-locked');
      control.removeAttribute('aria-disabled');
    });
  };

  // インラインの確認処理などが先に実行されるよう、バブリング段階で監視する。
  document.addEventListener('submit', event => {
    const form = event.target;
    if (!(form instanceof HTMLFormElement) || event.defaultPrevented) return;
    if ((form.method || 'get').toLowerCase() !== 'post') return;
    if (form.dataset.allowRepeatSubmit === 'true') return;

    if (form.dataset.submitLocked === 'true') {
      event.preventDefault();
      event.stopImmediatePropagation();
      return;
    }
    lockForm(form);
  });

  // 戻る操作でページがBFCacheから復元された場合は再操作できる状態に戻す。
  window.addEventListener('pageshow', () => {
    document.querySelectorAll('form[data-submit-locked="true"]').forEach(unlockForm);
  });
})();
