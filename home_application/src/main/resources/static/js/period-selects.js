document.addEventListener('DOMContentLoaded',()=>{
  const current=new Date().getFullYear();
  const replace=(input,values,suffix)=>{
    if(input.tagName!=='INPUT')return;
    const select=document.createElement('select');
    select.dataset.adaptiveDisabled='true';
    [...input.attributes].forEach(attribute=>{if(!['type','min','max','step','value'].includes(attribute.name))select.setAttribute(attribute.name,attribute.value)});
    values.forEach(value=>{const option=document.createElement('option');option.value=String(value);option.textContent=String(value)+suffix;option.selected=String(value)===String(input.value);select.append(option)});
    input.replaceWith(select);
  };
  document.querySelectorAll('input[name="year"]:not([type="hidden"])').forEach(input=>replace(input,Array.from({length:current-2023+3},(_,index)=>2023+index),'年'));
  document.querySelectorAll('input[name="month"]:not([type="hidden"])').forEach(input=>replace(input,Array.from({length:12},(_,index)=>index+1),'月'));
});
