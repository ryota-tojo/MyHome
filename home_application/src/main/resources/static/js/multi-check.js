document.addEventListener('DOMContentLoaded',()=>{
  document.querySelectorAll('.multi-check').forEach(widget=>{
    const toggle=widget.querySelector('.multi-check__toggle');
    const menu=widget.querySelector('.multi-check__menu');
    const cards=document.getElementById(widget.dataset.cardTarget);
    const choices=[...widget.querySelectorAll('.multi-check__choice')];
    const search=widget.querySelector('.multi-check__search');
    const render=()=>{
      cards.replaceChildren();
      choices.filter(choice=>choice.checked).forEach(choice=>{
        const card=document.createElement('article');card.className='selected-group-card';
        const heading=document.createElement('div');heading.className='selected-group-card__heading';
        const title=document.createElement('strong');title.textContent=choice.dataset.label;
        const id=document.createElement('small');id.textContent=choice.value;
        const close=document.createElement('button');close.type='button';close.className='selected-group-card__close';close.textContent='×';close.setAttribute('aria-label',choice.dataset.label+'を削除');
        close.addEventListener('click',()=>{choice.checked=false;render();});
        heading.append(title,id,close);
        const hr=document.createElement('hr');
        const role=document.createElement('select');role.name='role_'+choice.value;role.className='selected-group-card__role';
        role.innerHTML='<option value="0">メンバー</option><option value="1">リーダー</option>';
        card.append(heading,hr,role);cards.append(card);
      });
      toggle.textContent=choices.filter(choice=>choice.checked).length?choices.filter(choice=>choice.checked).length+'件選択中':'選択してください';
    };
    toggle.addEventListener('click',()=>{menu.hidden=!menu.hidden;toggle.setAttribute('aria-expanded',String(!menu.hidden));});
    choices.forEach(choice=>choice.addEventListener('change',render));
    search?.addEventListener('input',()=>{const words=search.value.normalize('NFKC').toLocaleLowerCase().split(/\s+/).filter(Boolean);choices.forEach(choice=>{const target=(choice.dataset.label+' '+choice.value).normalize('NFKC').toLocaleLowerCase();choice.closest('label').hidden=!words.every(word=>target.includes(word));});});
    search?.addEventListener('keydown',event=>{if(event.key==='Enter'&&!event.isComposing){event.preventDefault();event.stopPropagation();search.dispatchEvent(new Event('input'));}});
    document.addEventListener('click',event=>{if(!widget.contains(event.target)){menu.hidden=true;toggle.setAttribute('aria-expanded','false');}});
    render();
  });
});
