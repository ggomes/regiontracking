function plotFD()

close all

filedir = 'C:\Users\gomes\workspace\regiontracking\';
%inputfile = 'parabolicwithsignal.xml';
%inputfile = 'shortparabolic.xml';
%inputfile = 'input.xml';
%inputfile = 'input2.xml';
%inputfile = 'ex1.xml';
%inputfile = 'ex2.xml';
%inputfile = 'triangularwithsignal.xml';
inputfile = 'parabolicwithsignal.xml';

RegionTracking=xml_read(inputfile);

L = RegionTracking.LinkList.link;
clear RegionTracking

figure
for i=1:length(L)
    
    [rho,f] = evalFD(L(i).fd.ATTRIBUTE);
    
    plot(rho,f,'LineWidth',2)
    hold on
    
end
grid
xlabel('density [veh/mil]')
ylabel('flow [veh/hr]')




function [rho,f] = evalFD(FD)

fbar = FD.capacity;
rhocrit = FD.rhocrit;
rhojam = FD.rhojam;
type = FD.type;

rho = linspace(0,rhojam);

switch(type)
    case 'triangular'
        vf = fbar/rhocrit;
        w = fbar/(rhojam-rhocrit);
        f = min([rho*vf;w*(rhojam-rho)]);
    case 'parabolic'
        f = fbar.*rho.*(2*rhocrit-rho)./rhocrit./rhocrit;
end

