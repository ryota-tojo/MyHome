(() => {
  function text(element) { return (element?.textContent || '').replace(/\s+/g, '').trim(); }
  function highlight(table, headerName, value, className) {
    const headers = [...table.querySelectorAll('thead th')];
    const index = headers.findIndex(header => text(header).includes(headerName));
    if (index < 0) return;
    table.querySelectorAll('tbody tr').forEach(row => {
      const cells = row.querySelectorAll(':scope > td');
      if (cells[index] && text(cells[index]) === value) row.classList.add(className);
    });
  }
  function apply() {
    document.querySelectorAll('table').forEach(table => {
      highlight(table, 'グループ権限', 'リーダー', 'role-row-leader');
      highlight(table, '権限', '管理者', 'role-row-admin');
    });
  }
  document.readyState === 'loading' ? document.addEventListener('DOMContentLoaded', apply) : apply();
})();
