document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('details.panel').forEach((details, index) => {
    const form = details.querySelector('form[method="get"]');
    if (!form) return;

    const title = details.querySelector('summary')?.textContent?.trim() || '検索条件';
    const dialog = document.createElement('dialog');
    dialog.className = 'search-dialog';
    dialog.id = `search-dialog-${index}`;

    const heading = document.createElement('div');
    heading.className = 'search-dialog__heading';
    const h2 = document.createElement('h2');
    h2.textContent = title;
    const close = document.createElement('button');
    close.type = 'button';
    close.className = 'search-dialog__close';
    close.setAttribute('aria-label', '検索条件を閉じる');
    close.textContent = '×';
    heading.append(h2, close);

    form.classList.remove('form-grid');
    form.classList.add('search-dialog__form');
    dialog.append(heading, form);

    const open = document.createElement('button');
    open.type = 'button';
    open.className = 'secondary search-modal-open';
    open.textContent = '検索';
    open.setAttribute('aria-haspopup', 'dialog');
    open.setAttribute('aria-controls', dialog.id);

    details.replaceWith(open, dialog);
    open.addEventListener('click', () => dialog.showModal());
    close.addEventListener('click', () => dialog.close());
    dialog.addEventListener('click', event => {
      if (event.target === dialog) dialog.close();
    });
  });
});
