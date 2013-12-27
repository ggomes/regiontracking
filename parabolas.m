
fbar = 2400;
rhoc = 40;

rho = 10
f = fbar*rho*(2*rhoc-rho)/rhoc/rhoc

fprime =  2*fbar*(rhoc-rho)/rhoc/rhoc

f = 1050;
Sinv = rhoc*(1-sqrt(1-f/fbar))
Rinv = rhoc*(1+sqrt(1-f/fbar))