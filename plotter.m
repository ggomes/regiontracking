function []=plotter()

close all

filedir = 'C:\Users\gomes\workspace\regiontracking\';
inputfile = 'parabolicwithsignal.xml';
%inputfile = 'shortparabolic.xml';
%inputfile = 'input2.xml';
%inputfile = 'ex1.xml';
%inputfile = 'ex2.xml';
%inputfile = 'ex3.xml';
%inputfile = 'triangularwithsignal.xml';
inputfile = 'parabolicwithsignal.xml';

dt=NaN;
dx=NaN;
T=NaN;
L=NaN;
Nbar=NaN;
numcells=NaN;
maxdensity =NaN;
prefix={};

system(['java -jar regiontracking.jar ' filedir inputfile]);

% load simulation data
rho  = load([filedir 'out_density.txt']);
t = load([filedir 'out_t.txt']);
out_parameters()
any(any(isnan(rho)))
pcolorplot(['regions: ' inputfile])

for i=1:length(prefix)
    clear rho
    rho  = load([filedir prefix{i} '_density.txt']);
    eval([prefix{i} '_parameters()'])
    any(any(isnan(rho))) | any(any(rho<0))
    pcolorplot([prefix{i} ': ' inputfile])
end

% ======================================================
    function pcolorplot(tit)
        
        figure('Position',[306   255   572   384])
        x = 5280*(0:dx:L);
        xsize = min(length(x),size(rho,2));
        x = x(1:xsize);
        rho = rho(:,1:xsize);
        h=pcolor(x,t,rho);
        colormap(flipud(gray))
        set(h,'EdgeAlpha',0)
        caxis([0 maxdensity])
        colorbar()
        xlabel('distance [ft]')
        ylabel('time [sec]')
        %title(tit)
        
    end

display('done')


end
