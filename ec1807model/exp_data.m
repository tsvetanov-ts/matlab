
% format long; 
% close all; 
clear all

load ec1807kf
load ec1807k

ndx1 = find(ec1807kf(:, 1) >= 6.69 & ec1807kf(:, 1)<= 11.57);

tt = ec1807kf(ndx1,1);
Sd = ec1807kf(ndx1,2);

ndx2 = find(ec1807kf(:, 1) >= 7.224 & ec1807kf(:, 1)<= 11.57);
Sd(ndx2) = 2*0.08 - ec1807kf(ndx2,2);
Xd = ec1807kf(ndx1,3);
Fa = ec1807kf(ndx1,6);
Va = ec1807k(ndx1,6);
t0 = tt(1);
tf = tt(length(tt));

		
options = simset('solver', 'ode45', 'RelTol', 1e-4, 'AbsTol', 1e-6, 'MaxStep', 1);
TIMESPAN = [t0 tf];

[Tt, Xa, Y1, Y2] = sim('Model_XStest',  TIMESPAN, options, []);

tmod = Tt; Xmod = Y1; Smod = Y2;

 figure(1) 
 set(findall(gcf,'-property','FontSize'),'FontSize', 14)
 plot(tt, Xd, 'r', 'linewidth',2), grid, 
 hold on, plot(tmod, Xmod, 'b', 'linewidth',2) 
 legend('exp. data', 'model data', 'Location', 'northwest')
 title('Cultivation of E. coli MC4110'), xlabel('Time, [h]'), ylabel('Biomass, [g/l]')
 
 figure(2) 
 set(findall(gcf,'-property','FontSize'),'FontSize', 14)
 plot(tt, Sd, 'r', 'linewidth',2), grid, 
 hold on, plot(tmod, Smod, 'b', 'linewidth',2)
 legend('exp. data', 'model data')
 title('Cultivation of E. coli MC4110'), xlabel('Time, [h]'), ylabel('Substrate, [g/l]')
 
% [Tt1, Xa1, Y11, Y21] = sim('Model',  TIMESPAN, options, []);
% 
% tmod1 = Tt1; Xmod1 = Y11; Smod1 = Y21;
% 
%  figure(3) 
%  set(findall(gcf,'-property','FontSize'),'FontSize', 14)
%  plot(tt, Xd, 'r', 'linewidth',2), grid, 
%  hold on, plot(tmod1, Xmod1, 'b', 'linewidth',2) 
%  legend('exp. data', 'model data', 'Location', 'northwest')
%  title('Cultivation of E. coli MC4110'), xlabel('Time, [h]'), ylabel('Biomass, [g/l]')
%  
%  figure(4) 
%  set(findall(gcf,'-property','FontSize'),'FontSize', 14)
%  plot(tt, Sd, 'r', 'linewidth',2), grid, 
%  hold on, plot(tmod1, Smod1, 'b', 'linewidth',2)
%  legend('exp. data', 'model data')
%  title('Cultivation of E. coli MC4110'), xlabel('Time, [h]'), ylabel('Substrate, [g/l]')
%  
% 
