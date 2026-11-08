document.addEventListener('DOMContentLoaded',()=>{
  if(document.querySelector('.site-footer'))return;
  const version=document.querySelector('.latest-version-config')?.dataset.version||'-';
  const footer=document.createElement('footer');footer.className='site-footer';
  footer.innerHTML='<hr><span>Version '+version+'</span>';
  document.body.append(footer);
});
