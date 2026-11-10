document.addEventListener('DOMContentLoaded',()=>{
  const thresholdDefault=Number(document.querySelector('.ui-config')?.dataset.selectThreshold||8);
  const normalize=value=>(value||'').normalize('NFKC').toLocaleLowerCase();

  document.querySelectorAll('select').forEach((select,index)=>{
    if(select.dataset.adaptiveReady==='true')return;
    // 年月は候補数が多くても固定値なので、全画面で通常のselectに統一する。
    if(select.matches('[name="year"],[name="month"]')||select.dataset.adaptiveDisabled==='true')return;
    const threshold=Number(select.dataset.selectThreshold||thresholdDefault);
    const sourceOptions=[...select.options].filter(option=>option.value);
    const force=select.dataset.forceAdaptive==='true'||select.multiple;
    const chipsMode=select.multiple;
    if(!force&&sourceOptions.length<threshold)return;
    select.dataset.adaptiveReady='true';

    const combo=document.createElement('div');
    combo.className='adaptive-combo';
    const input=document.createElement('input');
    input.type='search';
    input.className='adaptive-combo__input';
    input.autocomplete='off';
    input.placeholder=select.dataset.searchPlaceholder||'検索・選択';
    input.setAttribute('role','combobox');
    input.setAttribute('aria-autocomplete','list');
    input.setAttribute('aria-expanded','false');
    const list=document.createElement('div');
    list.className='adaptive-combo__list';
    list.id='adaptive-combo-list-'+index;
    list.setAttribute('role','listbox');
    list.setAttribute('aria-multiselectable',String(select.multiple));
    input.setAttribute('aria-controls',list.id);
    combo.append(input,list);
    select.before(combo);
    const chips=document.createElement('div');
    if(chipsMode){chips.className='adaptive-combo__chips';chips.setAttribute('aria-label','選択済みラベル');combo.after(chips);}
    select.classList.add('adaptive-combo__native');

    const rows=sourceOptions.map(option=>{
      const row=document.createElement('button');
      row.type='button';
      row.className='adaptive-combo__option';
      row.dataset.search=normalize(option.textContent);
      row.setAttribute('role','option');
      const check=document.createElement('span');
      check.className='adaptive-combo__check';
      const text=document.createElement('span');
      text.textContent=option.textContent;
      if(select.multiple)row.append(check,text);
      else row.append(text);
      list.append(row);
      row.addEventListener('mousedown',event=>event.preventDefault());
      row.addEventListener('click',()=>{
        if(select.multiple)option.selected=!option.selected;
        else select.value=option.value;
        input.value='';
        filter('');
        select.dispatchEvent(new Event('change',{bubbles:true}));
        if(select.multiple){input.focus();input.select();}
        else close();
      });
      return {option,row};
    });

    const summary=()=>{
      const selected=sourceOptions.filter(option=>option.selected);
      if(chipsMode){
        if(document.activeElement!==input)input.value='';
        chips.replaceChildren(...selected.map(option=>{
          const chip=document.createElement('span');chip.className='adaptive-combo__chip';
          const text=document.createElement('span');text.textContent=option.textContent;
          const remove=document.createElement('button');remove.type='button';remove.className='adaptive-combo__chip-remove';remove.textContent='×';remove.setAttribute('aria-label',option.textContent+'の選択を解除');
          // chips は combo の外側に置かれているため、mousedown が document まで
          // 伝播すると close() が先に走り、click 前にボタンが再描画されてしまう。
          remove.addEventListener('mousedown',event=>{event.preventDefault();event.stopPropagation();});
          remove.addEventListener('click',event=>{event.preventDefault();event.stopPropagation();option.selected=false;select.dispatchEvent(new Event('change',{bubbles:true}));});
          chip.append(text,remove);return chip;
        }));
      }else if(document.activeElement!==input||!input.value){
        input.value=select.multiple?(selected.length?selected.map(x=>x.textContent).join('、'):''):(selected[0]?.textContent||'');
      }
      rows.forEach(({option,row})=>{row.classList.toggle('selected',option.selected);row.setAttribute('aria-selected',String(option.selected));});
    };
    const filter=query=>{
      const words=normalize(query).split(/\s+/).filter(Boolean);
      let visible=0;
      rows.forEach(({row})=>{const show=words.length===0||words.every(word=>row.dataset.search.includes(word));row.hidden=!show;row.style.display=show?'':'none';if(show)visible++;});
      list.classList.toggle('empty',visible===0);
    };
    const open=()=>{
      combo.classList.add('open');input.setAttribute('aria-expanded','true');
      if(input.value){input.select();filter('');}
    };
    const close=()=>{combo.classList.remove('open');input.setAttribute('aria-expanded','false');filter('');summary();};
    input.addEventListener('focus',open);
    input.addEventListener('click',open);
    input.addEventListener('input',()=>{open();filter(input.value);});
    input.addEventListener('keydown',event=>{if(event.key==='Enter'&&!event.isComposing){event.preventDefault();event.stopPropagation();open();filter(input.value);return;}if(event.key==='Escape'){event.preventDefault();close();input.blur();}if(event.key==='ArrowDown'){event.preventDefault();open();rows.find(x=>!x.row.hidden)?.row.focus();}});
    list.addEventListener('keydown',event=>{const visible=rows.map(x=>x.row).filter(x=>!x.hidden);const current=visible.indexOf(document.activeElement);if(event.key==='ArrowDown'){event.preventDefault();visible[Math.min(current+1,visible.length-1)]?.focus();}if(event.key==='ArrowUp'){event.preventDefault();current<=0?input.focus():visible[current-1]?.focus();}if(event.key==='Escape'){close();input.focus();}});
    select.addEventListener('change',()=>{input.value='';summary();});
    document.addEventListener('mousedown',event=>{if(!combo.contains(event.target)&&!chips.contains(event.target))close();});
    select.form?.addEventListener('reset',()=>setTimeout(summary));
    summary();
  });
});
