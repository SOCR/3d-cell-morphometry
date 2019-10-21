// fills holes in 3D TIFF volume
// tested with MATLAB R2014b
//
// 1st parameter - path to input TIFF volume
// 2nd parameter - path to output TIFF volume

function fillholes(in, out)		
		info = imfinfo(in);
		num_images = numel(info);

		for k = 1:num_images
			A = imread(in, k, 'Info', info);

			A = imfill(A, 'holes');

			imwrite(A, out, 'writemode', 'append', 'compression', 'none');
		end
end
