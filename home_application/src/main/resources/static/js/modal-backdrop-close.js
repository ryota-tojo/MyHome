document.addEventListener('DOMContentLoaded',()=>{
  document.querySelectorAll('dialog').forEach(dialog=>{
    if(dialog.dataset.backdropCloseReady==='true')return;
    dialog.dataset.backdropCloseReady='true';
    dialog.addEventListener('click',event=>{
      if(event.target!==dialog||!dialog.open)return;
      const hasEditableField=dialog.querySelector('input:not([type="hidden"]),select,textarea,[contenteditable="true"]');
      if(hasEditableField)return;
      const rect=dialog.getBoundingClientRect();
      const outside=event.clientX<rect.left||event.clientX>rect.right||event.clientY<rect.top||event.clientY>rect.bottom;
      if(outside)dialog.close();
    });
  });
});
