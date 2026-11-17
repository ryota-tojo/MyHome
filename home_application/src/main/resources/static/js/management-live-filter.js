document.addEventListener('DOMContentLoaded', () => {
  const normalize = value => (value || '')
    .normalize('NFKC')
    .toLocaleLowerCase('ja-JP')
    .replace(/\s+/g, ' ')
    .trim();

  document.querySelectorAll('[data-live-filter]').forEach(input => {
    const selector = input.dataset.liveFilter;
    const list = selector ? document.querySelector(selector) : null;
    if (!list) return;

    const emptyMessage = document.querySelector(`[data-filter-empty="${selector}"]`);
    const items = [...list.querySelectorAll('[data-filter-item]')];
    const storageKey = `my-home:live-filter:${location.pathname}:${selector}`;
    const retained = sessionStorage.getItem(storageKey);
    if (retained !== null) input.value = retained;
    const applyFilter = () => {
      const query = normalize(input.value);
      const words = query.split(' ').filter(Boolean);
      const filtering = words.length > 0;
      let visibleCount = 0;

      list.dataset.filterActive = String(filtering);
      list.classList.toggle('is-filtering', filtering);

      items.forEach(item => {
        const text = normalize(item.dataset.filterText || item.textContent);
        const visible = words.every(word => text.includes(word));
        item.hidden = !visible;
        if (visible) {
          item.style.removeProperty('display');
        } else {
          item.style.setProperty('display', 'none', 'important');
        }
        item.draggable = !filtering;
        if (visible) visibleCount += 1;
      });

      if (emptyMessage) {
        emptyMessage.hidden = !filtering || visibleCount !== 0;
        emptyMessage.style.display = filtering && visibleCount === 0 ? '' : 'none';
      }
      if (input.value) sessionStorage.setItem(storageKey, input.value);
      else sessionStorage.removeItem(storageKey);
    };

    input.addEventListener('input', applyFilter);
    input.addEventListener('search', applyFilter);
    applyFilter();
  });
});
