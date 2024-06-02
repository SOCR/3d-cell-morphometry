for i in ../data/astr_vpa/astr_vpa_original/Psi*.tif; do
  tmp=${i%%.tif}
  res=${tmp##*/}
  psf="../data/astr_vpa/psf/${res%%_c0*}_psf.tif"  
  java -jar DeconvolutionLab_2.jar Run -image file $i -psf file $psf -algorithm RL 10 -out stack $res -path ../data/astr_vpa/astr_vpa_decon
done
for i in ../data/astr_vpa/astr_vpa_original/Rho*.tif; do
  tmp=${i%%.tif}
  res=${tmp##*/}
  psf="../data/astr_vpa/psf/${res%%_c0*}_psf.tif"
  java -jar DeconvolutionLab_2.jar Run -image file $i -psf file $psf -algorithm RL 10 -out stack $res -path ../data/astr_vpa/astr_vpa_decon
done
