document.addEventListener('DOMContentLoaded',()=>{
  document.querySelectorAll('[data-touch-sortable]').forEach(list=>{
    const url=list.dataset.orderUrl;
    if(!url)return;
    let item=null,timer=null,active=false,startX=0,startY=0,pointerId=null;
    const cancelTimer=()=>{if(timer){clearTimeout(timer);timer=null;}};
    const finish=()=>{
      cancelTimer();
      if(active&&item){
        item.classList.remove('touch-sorting');
        document.body.classList.remove('touch-sort-active');
        const body=new URLSearchParams();
        [...list.children].forEach(child=>body.append('ids',child.dataset.id));
        const csrf=document.querySelector('input[name="_csrf"]');
        if(csrf)body.append(csrf.name,csrf.value);
        fetch(url,{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body}).then(response=>{if(!response.ok)location.reload();});
      }
      active=false;item=null;pointerId=null;
    };
    list.addEventListener('pointerdown',event=>{
      const handle=event.target.closest('.drag-handle');
      if(!handle||event.pointerType==='mouse')return;
      item=handle.closest('[data-id]');if(!item)return;
      pointerId=event.pointerId;startX=event.clientX;startY=event.clientY;
      timer=setTimeout(()=>{
        active=true;timer=null;item.classList.add('touch-sorting');document.body.classList.add('touch-sort-active');
        handle.setPointerCapture?.(pointerId);navigator.vibrate?.(30);
      },500);
    });
    list.addEventListener('contextmenu',event=>{if(event.target.closest('.drag-handle'))event.preventDefault();});
    list.addEventListener('pointermove',event=>{
      if(!item||event.pointerId!==pointerId)return;
      if(!active){if(Math.hypot(event.clientX-startX,event.clientY-startY)>10){cancelTimer();item=null;}return;}
      event.preventDefault();
      item.style.pointerEvents='none';
      const target=document.elementFromPoint(event.clientX,event.clientY)?.closest('[data-id]');
      item.style.pointerEvents='';
      if(!target||target===item||target.parentElement!==list)return;
      const rect=target.getBoundingClientRect();
      list.insertBefore(item,event.clientY<rect.top+rect.height/2?target:target.nextSibling);
    },{passive:false});
    list.addEventListener('pointerup',finish);
    list.addEventListener('pointercancel',finish);
  });
});
