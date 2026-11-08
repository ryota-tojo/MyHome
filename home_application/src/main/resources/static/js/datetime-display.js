document.addEventListener('DOMContentLoaded', () => {
  const timestampWithFraction = /(\d{4}-\d{2}-\d{2}[ T]\d{2}:\d{2}:\d{2})\.\d+/g;
  const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);
  const nodes = [];
  while (walker.nextNode()) nodes.push(walker.currentNode);
  nodes.forEach(node => {
    if (node.parentElement?.closest('script,style,textarea')) return;
    node.nodeValue = node.nodeValue.replace(timestampWithFraction, '$1');
  });
});
