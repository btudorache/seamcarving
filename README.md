# Seam Carving

**Seam carving** is an algorithm for content-aware image resizing. It functions by establishing a number of seams (paths of least importance) in an image and automatically removes seams to reduce image size or inserts seams to extend it. You can learn more about it on the [wiki page](https://en.wikipedia.org/wiki/Seam_carving).

## Usage

Compile and run with the following flags:

* ```-in input_file_path``` - specify the input file path

* ```-out output_file_path``` - specify the output file path

* ```-width width_length_resized``` - specify how many width pixels to remove

* ```-height height_length_resized``` - specify how many height pixels to remove

## Example

Image before applying Seam Carving:

![before](https://github.com/btudorache/seamcarving/blob/master/test/test2.png)

Image after removing 250 seams vertically and 250 seams horizontally:

![after](https://github.com/btudorache/seamcarving/blob/master/test/test2_out.png)
