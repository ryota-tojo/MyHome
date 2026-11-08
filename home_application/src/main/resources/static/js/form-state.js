document.addEventListener('DOMContentLoaded',()=>{
  const forms=[...document.querySelectorAll('form')].filter(f=>(f.method||'get').toLowerCase()!=='get');
  const hasError=new URLSearchParams(location.search).has('error')||document.querySelector('.alert,.error');
  forms.forEach((form,index)=>{
    const key='form-state:'+location.pathname+':'+(form.getAttribute('action')||location.pathname)+':'+index;
    if(hasError){try{const saved=JSON.parse(sessionStorage.getItem(key)||'{}');form.querySelectorAll('input,select,textarea').forEach(field=>{if(!field.name||field.type==='password'||field.type==='hidden'||field.name==='_csrf'||!(field.name in saved))return;if(field.type==='checkbox'||field.type==='radio')field.checked=Array.isArray(saved[field.name])?saved[field.name].includes(field.value):saved[field.name]===field.value;else field.value=saved[field.name]??'';});}catch(e){sessionStorage.removeItem(key)}}
    form.addEventListener('submit',()=>{const saved={};form.querySelectorAll('input,select,textarea').forEach(field=>{if(!field.name||field.type==='password'||field.type==='hidden'||field.name==='_csrf')return;if(field.type==='checkbox'||field.type==='radio'){if(!field.checked)return;(saved[field.name]??=[]).push(field.value);}else saved[field.name]=field.value;});sessionStorage.setItem(key,JSON.stringify(saved));});
  });
});
