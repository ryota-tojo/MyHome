document.addEventListener('DOMContentLoaded', () => {
  const transientKeys = new Set([
    'page', 'created', 'updated', 'saved', 'deleted', 'copied', 'settled',
    'unsettled', 'closed', 'reopened', 'bulkClosed', 'error', 'revalidate'
  ]);
  const forms = [...document.querySelectorAll('form')].filter(form =>
    (form.getAttribute('method') || 'get').toLowerCase() === 'get'
  );
  if (!forms.length) return;

  const storageKey = path => `my-home:search:${path}`;
  const formPath = form => {
    const url = new URL(form.getAttribute('action') || location.href, location.href);
    return url.origin === location.origin ? url.pathname : null;
  };
  const meaningful = source => {
    const result = new URLSearchParams();
    source.forEach((value, key) => {
      if (!transientKeys.has(key) && value !== '') result.append(key, value);
    });
    return result;
  };
  const save = (path, params) => {
    const filtered = meaningful(params);
    if (filtered.size) sessionStorage.setItem(storageKey(path), filtered.toString());
    else sessionStorage.removeItem(storageKey(path));
  };

  forms.forEach(form => form.addEventListener('submit', () => {
    const path = formPath(form);
    if (!path) return;
    save(path, new URLSearchParams(new FormData(form)));
  }));

  const currentPathHasSearch = forms.some(form => formPath(form) === location.pathname);
  if (currentPathHasSearch) {
    const current = new URLSearchParams(location.search);
    const currentSearch = meaningful(current);
    if (currentSearch.size) {
      save(location.pathname, currentSearch);
    } else {
      const saved = sessionStorage.getItem(storageKey(location.pathname));
      if (saved) {
        const restored = new URLSearchParams(saved);
        current.forEach((value, key) => {
          if (transientKeys.has(key) && key !== 'page') restored.append(key, value);
        });
        location.replace(`${location.pathname}?${restored}${location.hash}`);
        return;
      }
    }
  }

  document.addEventListener('click', event => {
    const control = event.target.closest('a,button,input[type="reset"]');
    if (!control) return;
    const label = (control.textContent || control.value || '').replace(/\s+/g, '');
    const explicitReset = control.matches('[data-search-reset],input[type="reset"]') || label === 'リセット' ||
      /検索条件.*(リセット|解除)|フィルタ.*解除|並び替え.*解除/.test(label);
    if (!explicitReset) return;
    const form = control.closest('form');
    const path = form ? formPath(form) : location.pathname;
    if (path) sessionStorage.removeItem(storageKey(path));
  });
});
