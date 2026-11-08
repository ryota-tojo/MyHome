document.addEventListener('DOMContentLoaded',()=>{
  document.querySelectorAll('label').forEach(label=>{
    if(!label.querySelector(':scope > input:required,:scope > select:required,:scope > textarea:required')||label.querySelector('.required,.required-badge'))return;
    const firstControl=label.querySelector(':scope > input,:scope > select,:scope > textarea');
    if(!firstControl)return;
    const explicitTitle=label.querySelector(':scope > strong,:scope > .field-label');
    const badge=document.createElement('span');badge.className='required-badge';badge.textContent='必須';
    if(explicitTitle){
      const heading=document.createElement('span');heading.className='label-title';
      explicitTitle.replaceWith(heading);
      heading.append(explicitTitle,badge);
      return;
    }
    const textNodes=[...label.childNodes].filter(node=>node.nodeType===Node.TEXT_NODE&&node.textContent.trim());
    const title=textNodes.map(node=>node.textContent.trim()).join(' ');
    if(!title)return;
    textNodes.forEach(node=>node.remove());
    const heading=document.createElement('span');heading.className='label-title';heading.append(document.createTextNode(title));heading.append(badge);
    label.insertBefore(heading,label.firstChild);
  });
});
