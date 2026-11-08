document.addEventListener("DOMContentLoaded", () => {
  document.querySelectorAll("input[type=color][data-color-target]").forEach((picker) => {
    const code = document.getElementById(picker.dataset.colorTarget);
    if (!code) return;
    picker.addEventListener("input", () => { code.value = picker.value.toUpperCase(); });
    code.addEventListener("input", () => {
      let value = code.value.trim();
      if (value && !value.startsWith("#")) value = `#${value}`;
      code.value = value.toUpperCase().slice(0, 7);
      if (/^#[0-9A-F]{6}$/.test(code.value)) picker.value = code.value;
    });
    code.addEventListener("blur", () => {
      if (/^#[0-9A-Fa-f]{6}$/.test(code.value)) code.value = code.value.toUpperCase();
    });
  });
});
