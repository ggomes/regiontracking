function animator()

close all
clear all

filedir = 'C:\Users\gomes\workspace\regiontracking\';
%inputfile = 'parabolicwithsignal.xml';
inputfile = 'triangularwithsignal.xml';
%inputfile = 'shortparabolic.xml';
%inputfile = 'input.xml';
%inputfile = 'input2.xml';

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
rho{1}  = load([filedir 'out_density.txt']);
t = load([filedir 'out_t.txt']);
out_parameters()
Dx{1} = dx;

for i=1:length(prefix)
    rho{i+1}  = load([filedir prefix{i} '_density.txt']);
    eval([prefix{i} '_parameters()'])
    Dx{i+1} = dx;
end

panimate(['regions: ' inputfile])

% ======================================================
    function panimate(tit)
        
        figure('Position',[306   140   791   499])

        for jj=1:length(rho)
            x{jj} = 5280*(0:Dx{jj}:L);
            xsize = min(length(x{jj}),size(rho{jj},2));
            x{jj} = x{jj}(1:xsize);
            rho{jj}= rho{jj}(:,1:xsize);
        end
        
        for jj=1:length(rho)
            if jj==1
               c = [0 0 0]; 
            else
               c = rand(1,3)
            end
            h(jj) = plot(x{jj},rho{jj}(1,:),'Color',c,'LineWidth',2);
            hold on
        end
        xlabel('distance [ft]')
        ylabel('time [sec]')
        title(tit)
        
        axis([0 x{1}(end) -0.1 maxdensity])
        z=textpos(0.9,0.9,0,'0.00',10);
        for ii=1:length(t)
            
            for jj=1:length(rho)
                set(h(jj),'YData',rho{jj}(ii,:));
            end
            axis([0 x{1}(end) -1 maxdensity*1.05])
            set(z,'String',num2str(t(ii),'%.2f'))
            pause(0.01)
        end
        
    end

display('done')


end
