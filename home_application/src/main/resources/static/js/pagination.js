document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('nav.pagination').forEach(nav => {
    const status = [...nav.querySelectorAll('span')].find(element => /\d+\s*\/\s*\d+/.test(element.textContent || ''));
    if (!status) return;

    const match = (status.textContent || '').match(/(\d+)\s*\/\s*(\d+)/);
    if (!match) return;
    const current = Number(match[1]);
    const pages = Number(match[2]);
    if (!Number.isInteger(current) || !Number.isInteger(pages) || pages < 1) return;

    const sampleLink = nav.querySelector('a[href]');
    let pageParameter = nav.dataset.pageParameter || 'page';
    if (sampleLink) {
      const sampleUrl = new URL(sampleLink.href, window.location.href);
      if (sampleUrl.searchParams.has('logPage')) pageParameter = 'logPage';
      else if (sampleUrl.searchParams.has('page')) pageParameter = 'page';
    }

    const pageUrl = page => {
      const url = new URL(window.location.href);
      url.searchParams.set(pageParameter, String(page));
      return `${url.pathname}${url.search}${url.hash}`;
    };
    const link = (label, page, className) => {
      const element = document.createElement('a');
      element.textContent = label;
      element.href = pageUrl(page);
      element.className = className;
      return element;
    };
    const disabled = label => {
      const element = document.createElement('span');
      element.textContent = label;
      element.className = 'page-control is-disabled';
      element.setAttribute('aria-disabled', 'true');
      return element;
    };

    const render = () => {
      const width = nav.clientWidth || window.innerWidth;
      const slots = width >= 650 ? 5 : width >= 430 ? 3 : 1;
      const visiblePages = Math.min(pages, slots);
      const half = Math.floor(visiblePages / 2);
      let start = Math.max(1, current - half);
      let end = Math.min(pages, start + visiblePages - 1);
      start = Math.max(1, end - visiblePages + 1);

      const elements = [];
      elements.push(current > 1 ? link('最初', 1, 'page-control') : disabled('最初'));
      elements.push(current > 1 ? link('前へ', current - 1, 'page-control') : disabled('前へ'));
      for (let page = start; page <= end; page += 1) {
        if (page === current) {
          const element = document.createElement('span');
          element.textContent = String(page);
          element.className = 'page-number is-current';
          element.setAttribute('aria-current', 'page');
          elements.push(element);
        } else {
          elements.push(link(String(page), page, 'page-number'));
        }
      }
      elements.push(current < pages ? link('次へ', current + 1, 'page-control') : disabled('次へ'));
      elements.push(current < pages ? link('最後へ', pages, 'page-control') : disabled('最後へ'));
      nav.replaceChildren(...elements);
    };

    render();
    if ('ResizeObserver' in window) {
      let previousWidth = nav.clientWidth;
      new ResizeObserver(entries => {
        const width = Math.round(entries[0].contentRect.width);
        if (width === previousWidth) return;
        previousWidth = width;
        render();
      }).observe(nav);
    }
  });
});
