document.addEventListener('DOMContentLoaded',()=>{
  const seconds=Number(document.querySelector('.ui-config')?.dataset.flashSeconds??5);
  if(!Number.isFinite(seconds)||seconds<=0)return;
  document.querySelectorAll('main .success').forEach(message=>{
    message.classList.add('flash-message');
    window.setTimeout(()=>{message.classList.add('flash-message--hiding');window.setTimeout(()=>message.remove(),320)},seconds*1000);
  });
});
