document.addEventListener('DOMContentLoaded',()=>{
  document.querySelectorAll('[data-touch-sortable]').forEach(list=>{
    const url=list.dataset.orderUrl;
    if(!url)return;
    let item=null,timer=null,active=false,startX=0,startY=0,pointerId=null,touchId=null;
    const cancelTimer=()=>{if(timer){clearTimeout(timer);timer=null}};
    const begin=(target,x,y)=>{
      item=target.closest('[data-id]');
      if(!item)return false;
      startX=x;startY=y;
      timer=setTimeout(()=>{
        active=true;timer=null;item.classList.add('touch-sorting');
        document.body.classList.add('touch-sort-active');navigator.vibrate?.(30);
      },350);
      return true;
    };
    const move=(x,y)=>{
      if(!item)return;
      if(!active){if(Math.hypot(x-startX,y-startY)>28){cancelTimer();item=null}return}
      item.style.pointerEvents='none';
      const target=document.elementFromPoint(x,y)?.closest('[data-id]');
      item.style.pointerEvents='';
      if(!target||target===item||target.parentElement!==list)return;
      const rect=target.getBoundingClientRect();
      list.insertBefore(item,y<rect.top+rect.height/2?target:target.nextSibling);
    };
    const finish=()=>{
      cancelTimer();
      if(active&&item){
        item.classList.remove('touch-sorting');document.body.classList.remove('touch-sort-active');
        const body=new URLSearchParams();
        [...list.children].forEach(child=>body.append('ids',child.dataset.id));
        const csrf=document.querySelector('input[name="_csrf"]');
        if(csrf)body.append(csrf.name,csrf.value);
        fetch(url,{method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded'},body})
          .then(response=>{if(!response.ok)location.reload()}).catch(()=>location.reload());
      }
      active=false;item=null;pointerId=null;touchId=null;
    };

    list.addEventListener('touchstart',event=>{
      const handle=event.target.closest('.drag-handle');
      if(!handle||event.touches.length!==1)return;
      event.preventDefault();const touch=event.touches[0];touchId=touch.identifier;
      begin(handle,touch.clientX,touch.clientY);
    },{passive:false});
    list.addEventListener('touchmove',event=>{
      const touch=[...event.touches].find(value=>value.identifier===touchId);
      if(!touch||!item)return;
      event.preventDefault();move(touch.clientX,touch.clientY);
    },{passive:false});
    list.addEventListener('touchend',event=>{
      if(touchId!==null&&![...event.touches].some(value=>value.identifier===touchId))finish();
    });
    list.addEventListener('touchcancel',finish);

    /* Touch Eventsと二重に動かないようPointer Eventsはペン入力だけ扱う。 */
    list.addEventListener('pointerdown',event=>{
      const handle=event.target.closest('.drag-handle');
      if(!handle||event.pointerType!=='pen')return;
      event.preventDefault();pointerId=event.pointerId;
      if(begin(handle,event.clientX,event.clientY))handle.setPointerCapture?.(pointerId);
    });
    list.addEventListener('pointermove',event=>{
      if(event.pointerId!==pointerId||!item)return;
      event.preventDefault();move(event.clientX,event.clientY);
    },{passive:false});
    list.addEventListener('pointerup',event=>{if(event.pointerId===pointerId)finish()});
    list.addEventListener('pointercancel',event=>{if(event.pointerId===pointerId)finish()});
    list.addEventListener('contextmenu',event=>{if(event.target.closest('.drag-handle'))event.preventDefault()});
  });
});
