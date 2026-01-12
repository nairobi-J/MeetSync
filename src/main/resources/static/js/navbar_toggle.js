const btn = document.getElementById("userProfileBtn");
const dropdown = document.getElementById("userProfileDropdown");

btn.addEventListener("click", () => {
    dropdown.classList.toggle("hidden");
});
