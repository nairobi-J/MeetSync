document.addEventListener('DOMContentLoaded', function() {
    
    // Mobile Menu Toggle
    const mobileMenuBtn = document.getElementById('mobileMenuBtn');
    const mobileMenu = document.getElementById('mobileMenu');
    
    if (mobileMenuBtn && mobileMenu) {
        mobileMenuBtn.addEventListener('click', function() {
            mobileMenu.classList.toggle('hidden');
            mobileMenu.classList.toggle('active');
            
            // Toggle icon
            const icon = this.querySelector('i');
            if (icon.classList.contains('fa-bars')) {
                icon.classList.remove('fa-bars');
                icon.classList.add('fa-times');
            } else {
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        });
    }
    
    // Smooth scroll for anchor links
    const anchorLinks = document.querySelectorAll('a[href^="#"]');
    anchorLinks.forEach(link => {
        link.addEventListener('click', function(e) {
            const href = this.getAttribute('href');
            if (href !== '#' && href !== '') {
                e.preventDefault();
                const target = document.querySelector(href);
                if (target) {
                    target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'start'
                    });
                    
                    // Close mobile menu if open
                    if (mobileMenu && !mobileMenu.classList.contains('hidden')) {
                        mobileMenu.classList.add('hidden');
                        mobileMenu.classList.remove('active');
                        const icon = mobileMenuBtn.querySelector('i');
                        icon.classList.remove('fa-times');
                        icon.classList.add('fa-bars');
                    }
                }
            }
        });
    });
    
    // Intersection Observer for fade-in animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };
    
    const observer = new IntersectionObserver(function(entries) {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in-up');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);
    
    // Observe sections for animation
    const sections = document.querySelectorAll('section');
    sections.forEach(section => {
        observer.observe(section);
    });
    
    // Demo calendar interaction (just for show)
    const calendarDays = document.querySelectorAll('.demo-calendar-day');
    const timeSlots = document.querySelectorAll('.demo-time-slot');
    
    calendarDays.forEach(day => {
        day.addEventListener('click', function() {
            // Remove active class from all days
            calendarDays.forEach(d => {
                d.classList.remove('bg-blue-600', 'text-white', 'rounded-full', 'font-semibold');
                d.classList.add('text-gray-700');
            });
            
            // Add active class to clicked day
            this.classList.add('bg-blue-600', 'text-white', 'rounded-full', 'font-semibold');
            this.classList.remove('text-gray-700');
            
            // Subtle animation for time slots
            timeSlots.forEach((slot, index) => {
                setTimeout(() => {
                    slot.style.animation = 'fadeInUp 0.3s ease-out';
                }, index * 50);
            });
        });
    });
    
    // Time slot click effect
    timeSlots.forEach(slot => {
        slot.addEventListener('click', function() {
            // Remove selected state from all slots
            timeSlots.forEach(s => {
                s.classList.remove('bg-blue-600', 'text-white');
                s.classList.add('border-blue-600', 'text-blue-600');
            });
            
            // Add selected state to clicked slot
            this.classList.remove('border-blue-600', 'text-blue-600');
            this.classList.add('bg-blue-600', 'text-white');
            
            // Show success message (could be replaced with actual booking)
            console.log('Time slot selected:', this.textContent);
        });
    });
    
    // Close mobile menu when clicking outside
    document.addEventListener('click', function(event) {
        if (mobileMenu && mobileMenuBtn) {
            const isClickInsideMenu = mobileMenu.contains(event.target);
            const isClickOnButton = mobileMenuBtn.contains(event.target);
            
            if (!isClickInsideMenu && !isClickOnButton && !mobileMenu.classList.contains('hidden')) {
                mobileMenu.classList.add('hidden');
                mobileMenu.classList.remove('active');
                const icon = mobileMenuBtn.querySelector('i');
                icon.classList.remove('fa-times');
                icon.classList.add('fa-bars');
            }
        }
    });
    
    // Add hover effect to feature cards
    const featureCards = document.querySelectorAll('[class*="border-2"]');
    featureCards.forEach(card => {
        card.classList.add('lift-on-hover');
    });
    
    // Add rotation effect to icons in "How it works" section
    const stepIcons = document.querySelectorAll('.group i');
    stepIcons.forEach(icon => {
        icon.classList.add('rotate-on-hover');
    });
    
});