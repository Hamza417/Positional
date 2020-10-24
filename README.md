# Positional

  A very flexible and customizable location related information app..

  ![banner](https://github.com/Hamza417/Positional/blob/master/poster.png?raw=false)

<br/>

## Stats

[![Hits](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FHamza417%2FPositional&count_bg=%233FA6E6&title_bg=%23FB062F&icon=github.svg&icon_color=%23FFFFFF&title=Total+Visited&edge_flat=false)](https://hits.seeyoufarm.com)
[![Download](https://badgen.net/badge/Download/v1.3-beta/grey?icon=https://svgshare.com/i/Qk3.svg)](https://github.com/Hamza417/Positional/releases/download/1.3-beta/1.3-beta.apk) [![Changelogs](https://badgen.net/badge/Changelogs/v1.3-beta/green?list)](https://github.com/Hamza417/Positional/releases/tag/1.3-beta)
![Status](https://badgen.net/badge/Status/beta/orange?icon)

<br/>

## Screenshots
<img src="https://github.com/Hamza417/Positional/blob/master/screenshots/scrsht_01.png" width="30%"> <img src="https://github.com/Hamza417/Positional/blob/master/screenshots/scrsht_02.png" width="30%"> <img src="https://github.com/Hamza417/Positional/blob/master/screenshots/scrsht_03.png" width="30%"> <img src="https://github.com/Hamza417/Positional/blob/master/screenshots/scrsht_04.png" width="30%"> <img src="https://github.com/Hamza417/Positional/blob/master/screenshots/scrsht_05.png" width="30%">

<br/>

## Features
  * Easy to use<br/>
  * Smooth, with fluid animations<br/>
  * Minimal UI<br/>
  * Parallax effect<br/>
  * Customisable with various options to choose from<br/>
  * Flexible, options for different elements can be chosen separately or can be disabled entirely<br/>
  * Torch

<br/>

## Features yet to be added

   #### Most Priority
  - [x] ~~GPS information panel~~
  - [ ] Dark Mode - Providing options for skins made this part a bit complicated
  - [x] ~~Speedometer~~
  - [x] ~~A separate fragment for torch~~ <br/>
        <sub>not feasible for a GPS app</sub>
  - [x] ~~A unified integrated location provider library~~
  - [x] ~~Swipe-able interface~~

   #### Least priority
  - [x] ~~Simplifying interface by adding few indicators referring the currently visible screen~~
  - [ ] Set of icons to choose from

<br/>

## Known Issues (so far)
  - [x] ~~Rotation causes view to move back to first panel~~
  - [ ] Parallax sensor values are wrong sometimes
  - [x] ~~Dialer rotating back to it's original state sometimes causes resource and input lock~~
  - [x] ~~Updating sensor values in the background **Handler Thread** to free up the UI load~~ <br/>
      <sub>**(21 Oct, 2020)** - ~~Sensor values cannot be updated in the background thread as the updating UI from the background causes a significant UI lag and communication delay~~ <br/> (24 Oct, 2020) - Sensor values are now updated in background thread<sub>
  
<br/>

## Contribution
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

<br/>

## Libraries Used
* [Material Popup Menu](https://github.com/zawadz88/MaterialPopupMenu) by **Piotr Zawadzki**
* [Material View Pager Dots Indicator](https://github.com/tommybuonomo/dotsindicator) by **Tommy Buonomo**
* [commons-suncalc](https://github.com/shred/commons-suncalc) by **Richard Körber**
* [Loader View for Android](https://github.com/elye/loaderviewlibrary) by **Elye**
  
<br/>

## License
  <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/">Creative Commons Attribution-NonCommercial 4.0 International License</a>.
