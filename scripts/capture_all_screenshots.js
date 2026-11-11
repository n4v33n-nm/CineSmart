const puppeteer = require('../frontend/node_modules/puppeteer-core');
const path = require('path');
const fs = require('fs');

const BASE_URL = 'http://localhost:5173';
const API_URL = 'http://localhost:8080/api';

const SCREENSHOTS_DIR = path.resolve(__dirname, '..', 'screenshots');
if (!fs.existsSync(SCREENSHOTS_DIR)) {
  fs.mkdirSync(SCREENSHOTS_DIR, { recursive: true });
}

async function loginUser(email, password) {
  try {
    const res = await fetch(`${API_URL}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    const data = await res.json();
    if (data.success && data.data?.accessToken) {
      return {
        token: data.data.accessToken,
        user: data.data.user
      };
    }
  } catch (err) {
    console.error(`Login error for ${email}:`, err);
  }
  return null;
}

async function setAuth(page, authData) {
  if (!authData) return;
  await page.evaluate(({ token, user }) => {
    localStorage.setItem('cinesmart_token', token);
    localStorage.setItem('cinesmart_user', JSON.stringify(user));
  }, authData);
}

async function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}

async function capture() {
  console.log('--- 1. Authenticating Users ---');
  const customerAuth = await loginUser('naveenkumar.cs24@krct.ac.in', 'Customer123!');
  console.log('Customer auth:', customerAuth ? `OK (${customerAuth.user.fullName})` : 'FAILED');

  const adminAuth = await loginUser('admin@cinesmart.com', 'Admin123!');
  console.log('Admin auth:', adminAuth ? `OK (${adminAuth.user.fullName})` : 'FAILED');

  console.log('\n--- 2. Launching Headless Chrome ---');
  const browser = await puppeteer.launch({
    executablePath: 'C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe',
    headless: 'new',
    defaultViewport: { width: 1440, height: 900, deviceScaleFactor: 1.5 },
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--disable-gpu']
  });

  const page = await browser.newPage();

  // Auto-handle any alerts or confirms so nothing hangs
  page.on('dialog', async dialog => {
    console.log(`[BROWSER DIALOG] ${dialog.type()}: ${dialog.message()}`);
    await dialog.accept();
  });

  async function snap(filename, desc) {
    const fullPath = path.join(SCREENSHOTS_DIR, filename);
    await page.screenshot({ path: fullPath, fullPage: false });
    console.log(`[SAVED] ${filename} - ${desc}`);
  }

  try {
    // ----------------------------------------------------
    // SCREENSHOT 01: Home Page
    // ----------------------------------------------------
    console.log('\nCapturing 01_HomePage.png...');
    await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(1500);
    await snap('01_HomePage.png', 'Home page with hero banner, navbar and featured movies');

    // ----------------------------------------------------
    // SCREENSHOT 02: Explore Movies
    // ----------------------------------------------------
    console.log('\nCapturing 02_ExploreMoviesPage.png...');
    await page.goto(`${BASE_URL}/movies`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(1500);
    await snap('02_ExploreMoviesPage.png', 'Explore Movies catalog with search and genre pills');

    // ----------------------------------------------------
    // SCREENSHOT 03: Movie Details & Showtimes
    // ----------------------------------------------------
    console.log('\nCapturing 03_MovieDetails_Showtimes.png...');
    await page.goto(`${BASE_URL}/movies/1`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(1500);
    await snap('03_MovieDetails_Showtimes.png', 'Movie details, synopsis, formats (IMAX 3D) and showtimes');

    // ----------------------------------------------------
    // SCREENSHOT 04: Real-Time Seat Selection & 5-Min Locking
    // ----------------------------------------------------
    console.log('\nCapturing 04_RealTime_Seat_Locking.png...');
    await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle0' });
    await setAuth(page, customerAuth);
    await page.goto(`${BASE_URL}/shows/1/seats`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(2000);

    // Select available seats
    await page.evaluate(() => {
      const seatButtons = Array.from(document.querySelectorAll('button[aria-label*="AVAILABLE"]'));
      if (seatButtons.length >= 2) {
        seatButtons[0].click();
        seatButtons[1].click();
      }
    });
    await sleep(1500);
    await snap('04_RealTime_Seat_Locking.png', 'Interactive seat layout with real-time 5-min locking countdown & group seating');

    // ----------------------------------------------------
    // SCREENSHOT 05: Booking Checkout, Coupons & Loyalty
    // ----------------------------------------------------
    console.log('\nCapturing 05_Booking_Checkout_Coupons_Loyalty.png...');
    await page.evaluate(() => {
      const btn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Proceed to Payment') || b.textContent.includes('Checkout'));
      if (btn) btn.click();
    });
    await sleep(1500);

    // In PaymentModal: apply coupon WELCOME50 and check loyalty checkbox
    await page.evaluate(() => {
      const couponInput = document.querySelector('input[placeholder*="coupon" i], input[placeholder*="code" i]');
      if (couponInput) {
        couponInput.value = 'WELCOME50';
        couponInput.dispatchEvent(new Event('input', { bubbles: true }));
      }
      const applyBtn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Apply'));
      if (applyBtn) applyBtn.click();

      const loyaltyCheckbox = document.querySelector('input[type="checkbox"]');
      if (loyaltyCheckbox && !loyaltyCheckbox.checked) {
        loyaltyCheckbox.click();
      }
    });
    await sleep(1200);
    await snap('05_Booking_Checkout_Coupons_Loyalty.png', 'Payment checkout modal with coupon discount and loyalty points redemption');

    // ----------------------------------------------------
    // SCREENSHOT 06: Digital Ticket & QR Code
    // ----------------------------------------------------
    console.log('\nCapturing 06_Digital_Ticket_QRCode.png...');
    await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle0' });
    await setAuth(page, customerAuth);
    await page.goto(`${BASE_URL}/bookings`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(2000);

    // Click "View Tickets" button on confirmed booking
    await page.evaluate(() => {
      const btn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('View Tickets'));
      if (btn) btn.click();
    });
    await sleep(1500);
    await snap('06_Digital_Ticket_QRCode.png', 'Digital Cinema E-Ticket with turnstile QR code, ticket code, seat and PDF download button');

    // Close the ticket modal if open
    await page.evaluate(() => {
      const closeBtn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.trim() === 'Close' || b.textContent.includes('×'));
      if (closeBtn) closeBtn.click();
    });
    await sleep(600);

    // ----------------------------------------------------
    // SCREENSHOT 07: Gate QR Ticket Scanner
    // ----------------------------------------------------
    console.log('\nCapturing 07_Gate_Ticket_Scanner.png...');
    await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle0' });
    await setAuth(page, adminAuth);
    await page.goto(`${BASE_URL}/verify-tickets`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(2000);

    // Type verified code and click verify to demonstrate scan result
    await page.evaluate(() => {
      const input = document.querySelector('input[placeholder*="TKT-"]');
      if (input) {
        input.value = 'TKT-2026-750E656A';
        input.dispatchEvent(new Event('input', { bubbles: true }));
      }
      const verifyBtn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Verify Ticket') || b.textContent.includes('Verify Pass'));
      if (verifyBtn) verifyBtn.click();
    });
    await sleep(1500);
    await snap('07_Gate_Ticket_Scanner.png', 'Gate ticket verification console with camera viewfinder, stats, and pass verification');

    // ----------------------------------------------------
    // SCREENSHOT 08: Customer Bookings & Reservations
    // ----------------------------------------------------
    console.log('\nCapturing 08_MyBookings_Reservations.png...');
    await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle0' });
    await setAuth(page, customerAuth);
    await page.goto(`${BASE_URL}/bookings`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(2000);
    await snap('08_MyBookings_Reservations.png', 'Customer reservations page with active bookings, tickets, and policy links');

    // ----------------------------------------------------
    // SCREENSHOT 09: Cancellation & Refund Modal
    // ----------------------------------------------------
    console.log('\nCapturing 09_Cancellation_Refund_Modal.png...');
    await page.evaluate(() => {
      // Find Cancel Booking button for booking 16 (CS-2026-933A3821)
      const cards = Array.from(document.querySelectorAll('.glass-panel'));
      for (const card of cards) {
        if (card.textContent.includes('CS-2026-933A3821') || card.textContent.includes('Confirmed')) {
          const cancelBtn = Array.from(card.querySelectorAll('button')).find(b => b.textContent.includes('Cancel Booking'));
          if (cancelBtn) {
            cancelBtn.click();
            return;
          }
        }
      }
      // Fallback
      const anyCancel = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Cancel Booking'));
      if (anyCancel) anyCancel.click();
    });
    await sleep(2000);
    await snap('09_Cancellation_Refund_Modal.png', 'Interactive Cancellation Modal with policy preview, penalty fee calculation, and loyalty reversals');

    // Close the cancellation modal
    await page.evaluate(() => {
      const keepBtn = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Keep Booking') || b.textContent.includes('Close'));
      if (keepBtn) keepBtn.click();
    });
    await sleep(600);

    // ----------------------------------------------------
    // SCREENSHOT 10: Refund Tracking Ledger
    // ----------------------------------------------------
    console.log('\nCapturing 10_Refund_Tracking_Ledger.png...');
    await page.evaluate(() => {
      const refundTab = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Refund Tracking'));
      if (refundTab) refundTab.click();
    });
    await sleep(1500);
    await snap('10_Refund_Tracking_Ledger.png', 'Customer refund tracking tab with statuses (REQUESTED, APPROVED, COMPLETED) & gateway references');

    // ----------------------------------------------------
    // SCREENSHOT 11: Loyalty Rewards Center
    // ----------------------------------------------------
    console.log('\nCapturing 11_Loyalty_Rewards_Center.png...');
    await page.goto(`${BASE_URL}/rewards`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(2000);
    await snap('11_Loyalty_Rewards_Center.png', 'Loyalty rewards center with tier badge, points balance, and transactions ledger');

    // ----------------------------------------------------
    // SCREENSHOT 12: Admin Movie Catalog Management
    // ----------------------------------------------------
    console.log('\nCapturing 12_Admin_Movie_Management.png...');
    await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle0' });
    await setAuth(page, adminAuth);
    await page.goto(`${BASE_URL}/admin`, { waitUntil: 'networkidle0', timeout: 15000 });
    await sleep(2000);
    await snap('12_Admin_Movie_Management.png', 'Admin Console - Movie Catalog management with add/deactivate controls');

    // ----------------------------------------------------
    // SCREENSHOT 13: Admin Show Scheduling
    // ----------------------------------------------------
    console.log('\nCapturing 13_Admin_Show_Scheduling.png...');
    await page.evaluate(() => {
      const showsTab = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Shows'));
      if (showsTab) showsTab.click();
    });
    await sleep(1500);
    await snap('13_Admin_Show_Scheduling.png', 'Admin Console - Show schedules, auditorium assignments, and seat inventory');

    // ----------------------------------------------------
    // SCREENSHOT 14: Admin Promotional Coupons
    // ----------------------------------------------------
    console.log('\nCapturing 14_Admin_Coupon_Management.png...');
    await page.evaluate(() => {
      const couponsTab = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Coupons'));
      if (couponsTab) couponsTab.click();
    });
    await sleep(1500);
    await snap('14_Admin_Coupon_Management.png', 'Admin Console - Promotional coupon campaigns, discount rules, and usage caps');

    // ----------------------------------------------------
    // SCREENSHOT 15: Admin Refund Management
    // ----------------------------------------------------
    console.log('\nCapturing 15_Admin_Refund_Management.png...');
    await page.evaluate(() => {
      const refundsTab = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Refunds'));
      if (refundsTab) refundsTab.click();
    });
    await sleep(1500);
    await snap('15_Admin_Refund_Management.png', 'Admin Console - Refund request review, simulated gateway disbursement, and rejection actions');

    // ----------------------------------------------------
    // SCREENSHOT 16: Admin Cancellation Policies
    // ----------------------------------------------------
    console.log('\nCapturing 16_Admin_Cancellation_Policies.png...');
    await page.evaluate(() => {
      const policiesTab = Array.from(document.querySelectorAll('button')).find(b => b.textContent.includes('Policies'));
      if (policiesTab) policiesTab.click();
    });
    await sleep(1500);
    await snap('16_Admin_Cancellation_Policies.png', 'Admin Console - Configurable tiered cancellation policies with refund percentages and fee schedules');

    console.log('\n=============================================');
    console.log('All 16 high-resolution screenshots generated successfully!');
    console.log('Location: ' + SCREENSHOTS_DIR);
    console.log('=============================================');

  } catch (err) {
    console.error('Error during capture:', err);
  } finally {
    await browser.close();
  }
}

capture();
